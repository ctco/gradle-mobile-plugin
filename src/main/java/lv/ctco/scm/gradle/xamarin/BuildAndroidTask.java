/*
 * @(#)BuildAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.gradle.utils.PropertyUtil;
import lv.ctco.scm.mobile.utils.AndroidApksignerUtil;
import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.ZipUtil;
import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.LoggerOutputStream;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildAndroidTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(BuildAndroidTask.class);

    private Environment env;

    private File projectFile;

    private String signingKeystore;
    private String signingCertificateAlias;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public void setSigningKeystore(String signingKeystore) {
        this.signingKeystore = signingKeystore;
    }

    public void setSigningCertificateAlias(String signingCertificateAlias) {
        this.signingCertificateAlias = signingCertificateAlias;
    }

    private String getProjectName() {
        return projectFile.getParentFile().getName();
    }

    private File getConfigurationBinDir() {
        return new File(getProjectName()+"/bin/"+env.getConfiguration());
    }

    @TaskAction
    public void doTaskAction() {
        try {
            buildArtifact();
            if (!"debug".equalsIgnoreCase(env.getConfiguration()) && signingCertificateAlias != null) {
                verifyArtifactSignature();
            }
            moveArtifactToDistDir();
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private void buildArtifact() throws IOException {
        ExecCommand execCommand = new ExecCommand("msbuild");
        execCommand.addArgument("/property:Configuration="+env.getConfiguration());
        if (signingCertificateAlias != null && !signingCertificateAlias.isEmpty()) {
            String storepass = "";
            if (PropertyUtil.hasProjectProperty(getProject(), "android.storepass")) {
                storepass = PropertyUtil.getProjectProperty(getProject(), "android.storepass");
            } else {
                throw new IOException("Android signing store pass 'android.storepass' has not been provided");
            }
            String keypass = "";
            if (PropertyUtil.hasProjectProperty(getProject(), "android.keypass")) {
                keypass = PropertyUtil.getProjectProperty(getProject(), "android.keypass");
            } else {
                throw new IOException("Android signing key pass 'android.keypass' has not been provided");
            }
            if (signingKeystore == null) {
                signingKeystore = new File(FileUtils.getUserDirectory(), ".gradle/init.d/android.keystore").getCanonicalPath();
            } else {
                signingKeystore = (new File(signingKeystore.replace("~", System.getProperty("user.home")))).getCanonicalPath();
            }
            //
            execCommand.addArgument("/property:AndroidKeyStore=True");
            execCommand.addArgument("/property:AndroidSigningKeyStore="+signingKeystore);
            execCommand.addArgument("/property:AndroidSigningStorePass="+storepass);
            execCommand.addArgument("/property:AndroidSigningKeyAlias="+signingCertificateAlias);
            execCommand.addArgument("/property:AndroidSigningKeyPass="+keypass);
        }
        execCommand.addArgument("/target:SignAndroidPackage");
        execCommand.addArgument("/consoleLoggerParameters:NoSummary");
        execCommand.addArgument(projectFile.getAbsolutePath(), false);
        logger.debug("{}", execCommand);
        ExecResult execResult = ExecUtil.executeCommand(execCommand, new LoggerOutputStream());
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    private void verifyArtifactSignature() throws IOException {
        Path projectDir = getProject().getProjectDir().toPath();
        Path targetApk = findSignedArtifact().getCanonicalFile().toPath();
        logger.info("Verifying signature for '{}'...", projectDir.relativize(targetApk));
        ExecResult execResult = AndroidApksignerUtil.verify(targetApk.toFile());
        for (String line : execResult.getOutput()) {
            logger.info(line);
        }
        if (!execResult.isSuccess()) {
            throw new IOException("Signature verification failed for apk file", execResult.getException());
        }
    }

    private File findSignedArtifact() throws IOException {
        List<File> files = CommonUtil.findAndroidAppsInDirectory(getConfigurationBinDir());
        for (File apk : files) {
            if (apk.getName().toLowerCase().endsWith("-signed.apk")) {
                return apk;
            }
        }
        throw new IOException("Expected APK was not found in build directory!");
    }

    private void moveArtifactToDistDir() throws IOException {
        File apkDistDir = new File(getProject().getBuildDir(),"apkdist");
        File sourceApk = findSignedArtifact();
        File targetApk = new File(apkDistDir, getProjectName()+" "+env.getName().toUpperCase()+".apk");
        FileUtils.copyFile(sourceApk, targetApk);
        FileUtils.forceDelete(sourceApk);
        //
        try (Stream<Path> stream = Files.list(getConfigurationBinDir().toPath())) {
            List<Path> msyms = stream
                    .filter(Files::isDirectory)
                    .filter(dirpath -> dirpath.endsWith(".mSYM"))
                    .collect(Collectors.toList());
            msyms.remove(getConfigurationBinDir().toPath());
            if (msyms.size() == 1) {
                File msymDir = msyms.get(0).toFile();
                File msymDistDir = new File(getProject().getBuildDir(), "msymdist");
                ZipUtil.compressDirectory(msymDir, true, new File(msymDistDir, getProjectName()+" "+env.getName()+".mSYM.zip"));
                FileUtils.deleteDirectory(msymDir);
            } else {
                logger.warn("None or multiple MSYMs found! Not moving to distribution folder.");
            }
        }
    }

}
