/*
 * @(#)BuildAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.objects.Environment;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BuildAndroidTask extends DefaultTask {

    private Environment env;

    private File projectFile;
    private String projectName;
    private String assemblyName;

    private String javaXmx;
    private String javaOpts;

    private String signingKeystore;
    private String signingCertificateAlias;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public void setJavaXmx(String javaXmx) {
        this.javaXmx = javaXmx;
    }

    public void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
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
            buildArtifact();
            if (!"debug".equalsIgnoreCase(env.getConfiguration()) && signingCertificateAlias != null) {
                signArtifact();
                verifyArtifact();
                zipalignArtifact();
            }
            moveArtifactToDistDir();
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void buildArtifact() throws IOException {
        CommandLine commandLine = new CommandLine("xbuild");
        commandLine.addArgument("/t:SignAndroidPackage");
        if (StringUtils.isNotBlank(env.getConfiguration())) {
             commandLine.addArgument("/p:Configuration="+env.getConfiguration());
        }
        if (StringUtils.isNotBlank(javaXmx)) {
            commandLine.addArgument("/p:JavaMaximumHeapSize="+javaXmx);
        }
        if (StringUtils.isNotBlank(javaOpts)) {
            commandLine.addArgument("/p:JavaOptions="+javaOpts);
        }
        commandLine.addArgument(projectFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
        FileUtils.writeLines(new File(PathUtil.getBuildlogDir(), this.getName()+"Task.build.log"), execResult.getOutput());
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    private void signArtifact() throws IOException {
        LoggerUtil.info("Signing package...");
        String storepass = "";
        if (PropertyUtil.hasProjectProperty("android.storepass")) {
            storepass = PropertyUtil.getProjectProperty("android.storepass");
        }
        String keypass = "";
        if (PropertyUtil.hasProjectProperty("android.keypass")) {
            keypass = PropertyUtil.getProjectProperty("android.keypass");
        }
        if (signingKeystore == null) {
            signingKeystore = PathUtil.getAndroidKeystore().getAbsolutePath();
        } else {
            signingKeystore = (new File(signingKeystore.replace("~", System.getProperty("user.home")))).getAbsolutePath();
        }
        CommandLine commandLine = new CommandLine("jarsigner");
        commandLine.addArgument("-verbose");
        commandLine.addArgument("-digestalg");
        commandLine.addArgument("SHA1");
        commandLine.addArgument("-sigalg");
        commandLine.addArgument("MD5withRSA");
        commandLine.addArgument("-keystore");
        commandLine.addArgument(signingKeystore, false);
        commandLine.addArgument("-storepass");
        commandLine.addArgument(storepass);
        commandLine.addArgument("-keypass");
        commandLine.addArgument(keypass);
        commandLine.addArgument(projectName+"/bin/"+env.getConfiguration()+"/"+assemblyName+".apk");
        commandLine.addArgument(signingCertificateAlias);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException("Signing for "+env.getUpperCaseName()+" failed");
        }
        LoggerUtil.info("Signing successful.");
    }

    private void verifyArtifact() throws IOException {
        LoggerUtil.info("Verifying artifact signature...");
        CommandLine commandLine = new CommandLine("jarsigner");
        commandLine.addArgument("-verify");
        commandLine.addArgument("-verbose");
        commandLine.addArgument(projectName+"/bin/"+env.getConfiguration()+"/"+assemblyName+".apk", false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        if (!execResult.isSuccess() || !execResult.getOutput().contains("jar verified.")) {
            throw new IOException("Artifact signature verification failed");
        } else {
            LoggerUtil.info("Artifact signature verification successful");
        }
    }

    private void zipalignArtifact() throws IOException {
        LoggerUtil.info("Zipaligning artifact...");
        File zipalign = PathUtil.getZipalignBinary();
        if (zipalign == null) {
            LoggerUtil.errorInTask(this.getName(), "Artifact signature verification failed");
            throw new GradleException("Zipalign binary was not found!");
        }
        CommandLine commandLine = new CommandLine(zipalign.getAbsolutePath());
        commandLine.addArgument("-f");
        commandLine.addArgument("-v");
        commandLine.addArgument("4");
        commandLine.addArgument(projectName+"/bin/"+env.getConfiguration()+"/"+assemblyName+".apk", false);
        commandLine.addArgument(projectName+"/bin/"+env.getConfiguration()+"/"+assemblyName+"-Signed.apk", false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new GradleException("Zipaligning failed");
        }
        Files.deleteIfExists(new File(projectName+"/bin/"+env.getName()+"/"+assemblyName+".apk").toPath());
        LoggerUtil.info("Zipaligning successful");
    }

    private void moveArtifactToDistDir() throws IOException {
        File apkDistDir = PathUtil.getApkDistDir();
        File sourceApkFile = new File(env.getOutputPath(), assemblyName+"-Signed.apk");
        File targetApkFile = new File(apkDistDir, assemblyName+" "+env.getUpperCaseName()+".apk");
        FileUtils.copyFile(sourceApkFile, targetApkFile);
        FileUtils.forceDelete(sourceApkFile);
    }

}
