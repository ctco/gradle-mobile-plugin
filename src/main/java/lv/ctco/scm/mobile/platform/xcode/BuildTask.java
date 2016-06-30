/*
 * @(#)BuildTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.objects.Environment;
import lv.ctco.scm.mobile.core.objects.IosApp;
import lv.ctco.scm.mobile.core.utils.BuildReportUtil;
import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.ZipUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BuildTask extends DefaultTask {

    private Environment env;

    public void setEnv(Environment env) {
        this.env = env;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            buildArtifacts();
            if (isIphoneosApplicationBuild()) {
                checkArtifacts();
                moveArtifactsToDistDir();
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

    private boolean isIphoneosApplicationBuild() throws IOException {
        return "iphoneos".equalsIgnoreCase(env.getSdk()) && "com.apple.product-type.application".equalsIgnoreCase(XcodeUtil.getProductType(env.getTarget()));
    }

    private void buildArtifacts() throws IOException {
        CommandLine commandLine = new CommandLine("xcodebuild");
        commandLine.addArgument("-configuration");
        commandLine.addArgument(env.getConfiguration());
        commandLine.addArgument("-sdk");
        commandLine.addArgument(env.getSdk());
        commandLine.addArgument("-target");
        commandLine.addArgument(env.getTarget(), false);
        commandLine.addArgument("DSTROOT="+PathUtil.getXcodeDstDir().getAbsolutePath());
        commandLine.addArgument("OBJROOT="+PathUtil.getXcodeObjDir().getAbsolutePath());
        commandLine.addArgument("SYMROOT="+new File(PathUtil.getXcodeSymDir(), env.getName()).getAbsolutePath());
        commandLine.addArgument("SHARED_PRECOMPS_DIR="+PathUtil.getXcodeSharedDir().getAbsolutePath());
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
        FileUtils.writeLines(new File(PathUtil.getBuildlogDir(), this.getName()+"Task.build.log"), execResult.getOutput());
        if (!execResult.isSuccess()) {
            LoggerUtil.errorInTask(this.getName(), execResult.getException().getMessage());
            throw new GradleException(execResult.getException().getMessage());
        }
    }

    private void checkArtifacts() throws IOException {
        File buildDir = new File(PathUtil.getXcodeSymDir(), env.getName()+"/"+env.getConfiguration()+"-"+env.getSdk());
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(buildDir);
        if (apps.size() == 1) {
            appDir = apps.get(0);
        } else {
            throw new IOException("None or multiple apps found in build directory!");
        }
        IosApp iosApp = new IosApp(appDir, env);
        BuildReportUtil.addIosAppInfo(iosApp);
        if (iosApp.isSignedWithDeveloperIdentity()
                && (!"Debug".equalsIgnoreCase(env.getConfiguration()) || !"UITests".equalsIgnoreCase(env.getConfiguration()))) {
            throw new IOException("iPhone Developer identity is not allowed for "+env.getConfiguration()+" configuration!");
        }
    }

    private void moveArtifactsToDistDir() throws IOException {
        File buildDir = new File(PathUtil.getXcodeSymDir(), env.getName()+"/"+env.getConfiguration()+"-"+env.getSdk());
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(buildDir);
        if (apps.size() == 1) {
            appDir = apps.get(0);
        } else {
            throw new IOException("None or multiple apps found in build directory");
        }
        String appName = FilenameUtils.getBaseName(appDir.getName());
        File payloadDir = new File(buildDir, "Payload");
        if (payloadDir.exists()) {
            FileUtils.deleteDirectory(payloadDir);
        }
        FileUtils.moveDirectoryToDirectory(appDir, payloadDir, true);
        if (!StringUtils.endsWithIgnoreCase(appName, env.getUpperCaseName())) {
            appName = appName+" "+env.getUpperCaseName();
        }
        File ipaFile = new File(PathUtil.getIpaDistDir(), appName+".ipa");
        ZipUtil.compressDirectory(new File(buildDir, "Payload"), true, ipaFile);
        FileUtils.deleteDirectory(payloadDir);
        //
        List<File> dsyms = CommonUtil.findIosDsymsinDirectory(buildDir);
        if (dsyms.size() == 1 ) {
            File dsymDir = dsyms.get(0);
            ZipUtil.compressDirectory(dsymDir, true, new File(PathUtil.getDsymDistDir(), "dSYM."+env.getName()+".zip"));
            FileUtils.deleteDirectory(dsymDir);
        } else {
            LoggerUtil.warn("None or multiple DSYMs found! Not moving to distribution folder.");
        }
    }

}
