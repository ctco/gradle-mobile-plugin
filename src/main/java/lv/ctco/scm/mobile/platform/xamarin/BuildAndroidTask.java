/*
 * @(#)BuildAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class BuildAndroidTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(BuildAndroidTask.class);

    private Environment env;

    private File projectFile;
    private String projectName;

    private String signingKeystore;
    private String signingCertificateAlias;

    private String configurationBinPath;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSigningKeystore(String signingKeystore) {
        this.signingKeystore = signingKeystore;
    }

    public void setSigningCertificateAlias(String signingCertificateAlias) {
        this.signingCertificateAlias = signingCertificateAlias;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            configurationBinPath = projectName+"/bin/"+env.getConfiguration();
            buildArtifact();
            if (!"debug".equalsIgnoreCase(env.getConfiguration()) && signingCertificateAlias != null) {
                signArtifact();
                verifyArtifact();
                zipalignArtifact();
            }
            moveArtifactToDistDir();
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private void buildArtifact() throws IOException {
        CommandLine commandLine = new CommandLine("xbuild");
        commandLine.addArgument("/t:SignAndroidPackage");
        if (StringUtils.isNotBlank(env.getConfiguration())) {
             commandLine.addArgument("/p:Configuration="+env.getConfiguration());
        }
        commandLine.addArgument(projectFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
        FileUtils.writeLines(new File(PathUtil.getBuildlogDir(), this.getName()+"Task.build.log"), execResult.getOutput());
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    private void signArtifact() throws IOException {
        logger.info("Codesigning package...");
        String storepass = "";
        if (PropertyUtil.hasProjectProperty(getProject(), "android.storepass")) {
            storepass = PropertyUtil.getProjectProperty(getProject(), "android.storepass");
        }
        String keypass = "";
        if (PropertyUtil.hasProjectProperty(getProject(), "android.keypass")) {
            keypass = PropertyUtil.getProjectProperty(getProject(), "android.keypass");
        }
        if (signingKeystore == null) {
            signingKeystore = PathUtil.getAndroidKeystore().getAbsolutePath();
        } else {
            signingKeystore = (new File(signingKeystore.replace("~", System.getProperty("user.home")))).getAbsolutePath();
        }
        Files.deleteIfExists(getSignedArtifact().toPath());
        CommandLine commandLine = new CommandLine("jarsigner");
        commandLine.addArgument("-verbose");
        commandLine.addArgument("-digestalg");
        commandLine.addArgument("SHA1");
        commandLine.addArgument("-sigalg");
        commandLine.addArgument("SHA1withRSA");
        commandLine.addArgument("-keystore");
        commandLine.addArgument(signingKeystore, false);
        commandLine.addArgument("-storepass");
        commandLine.addArgument(storepass);
        commandLine.addArgument("-keypass");
        commandLine.addArgument(keypass);
        commandLine.addArgument(getUnsignedArtifact().getAbsolutePath());
        commandLine.addArgument(signingCertificateAlias);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException("Signing for "+env.getName().toUpperCase()+" failed");
        }
        logger.info("Codesigning done.");
    }

    private void verifyArtifact() throws IOException {
        logger.info("Verifying artifact signature...");
        CommandLine commandLine = new CommandLine("jarsigner");
        commandLine.addArgument("-verify");
        commandLine.addArgument("-verbose");
        commandLine.addArgument(getUnsignedArtifact().getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        if (!execResult.isSuccess() || !execResult.getOutput().contains("jar verified.")) {
            throw new IOException("Artifact signature verification failed");
        } else {
            logger.info("Artifact signature verification successful.");
        }
    }

    private void zipalignArtifact() throws IOException {
        logger.info("Zipaligning artifact...");
        File sourceApk = getUnsignedArtifact();
        File targetApk = new File(sourceApk.getParentFile(), sourceApk.getName().substring(0,sourceApk.getName().length()-4)+"-signed.apk");
        CommandLine commandLine = new CommandLine("zipalign");
        commandLine.addArgument("-f");
        commandLine.addArgument("-v");
        commandLine.addArgument("4");
        commandLine.addArgument(sourceApk.getAbsolutePath(), false);
        commandLine.addArgument(targetApk.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new GradleException("Zipaligning failed");
        }
        Files.deleteIfExists(sourceApk.toPath());
        FileUtils.copyFile(targetApk, sourceApk);
        logger.info("Zipaligning successful");
    }

    private File getUnsignedArtifact() throws IOException {
        List<File> files = CommonUtil.findAndroidAppsInDirectory(new File(configurationBinPath));
        for (File apk : files) {
            if (!apk.getName().toLowerCase().endsWith("-signed.apk")) {
                return apk;
            }
        }
        throw new IOException("Expected APK was not found in build directory!");
    }

    private File getSignedArtifact() throws IOException {
        List<File> files = CommonUtil.findAndroidAppsInDirectory(new File(configurationBinPath));
        for (File apk : files) {
            if (apk.getName().toLowerCase().endsWith("-signed.apk")) {
                return apk;
            }
        }
        throw new IOException("Expected APK was not found in build directory!");
    }

    private void moveArtifactToDistDir() throws IOException {
        Files.deleteIfExists(getUnsignedArtifact().toPath());
        File apkDistDir = PathUtil.getApkDistDir();
        File sourceApk = getSignedArtifact();
        File targetApk;
        String apkName;
        if (projectName == null) {
            apkName = sourceApk.getName().substring(0, sourceApk.getName().length()-11);
        } else {
            apkName = projectName;
        }
        targetApk = new File(apkDistDir, apkName+" "+env.getName().toUpperCase()+".apk");
        FileUtils.copyFile(sourceApk, targetApk);
        FileUtils.forceDelete(sourceApk);
    }

}
