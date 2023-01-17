/*
 * @(#)BuildTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.mobile.utils.IosApp;

import lv.ctco.scm.mobile.utils.BuildReportUtil;
import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.ExecResult;
import lv.ctco.scm.mobile.utils.ExecUtil;
import lv.ctco.scm.mobile.utils.ZipUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BuildTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(BuildTask.class);

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
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private boolean isIphoneosApplicationBuild() throws IOException {
        return "iphoneos".equalsIgnoreCase(env.getSdk()) && "com.apple.product-type.application".equalsIgnoreCase(XcodeUtil.getProductType(env.getTarget()));
    }

    private void buildArtifacts() throws IOException {
        CommandLine commandLine = new CommandLine("xcodebuild");
        commandLine.addArgument("-configuration");
        commandLine.addArgument(env.getConfiguration(), false);
        commandLine.addArgument("-sdk");
        commandLine.addArgument(env.getSdk());
        commandLine.addArgument("-target");
        commandLine.addArgument(env.getTarget(), false);
        commandLine.addArgument("DSTROOT="+new File(getProject().getBuildDir(), "xcodebuild/dst").getAbsolutePath());
        commandLine.addArgument("OBJROOT="+new File(getProject().getBuildDir(), "xcodebuild/obj").getAbsolutePath());
        commandLine.addArgument("SYMROOT="+new File(new File(getProject().getBuildDir(), "xcodebuild/sym"), env.getName()).getAbsolutePath());
        commandLine.addArgument("SHARED_PRECOMPS_DIR="+new File(getProject().getBuildDir(), "xcodebuild/shared").getAbsolutePath());
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

    private void checkArtifacts() throws IOException {
        File buildDir = new File(new File(getProject().getBuildDir(), "xcodebuild/sym"), env.getName()+"/"+env.getConfiguration()+"-"+env.getSdk());
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(buildDir);
        if (apps.size() == 1) {
            appDir = apps.get(0);
        } else {
            throw new IOException("None or multiple apps found in build directory!");
        }
        IosApp iosApp = new IosApp(appDir);
        iosApp.setName(env.getName());
        iosApp.setBuildCnf(env.getConfiguration());
        iosApp.setBuildSdk(env.getSdk());
        BuildReportUtil.addIosAppInfo(iosApp);
        if (iosApp.isSignedWithDeveloperIdentity()) {
            if (!("Debug".equalsIgnoreCase(env.getConfiguration()))) {
                throw new IOException("iPhone Developer identity is not allowed for "+env.getConfiguration()+" configuration");
            }
        }
    }

    private void moveArtifactsToDistDir() throws IOException {
        File buildDir = new File(new File(getProject().getBuildDir(), "xcodebuild/sym"), env.getName()+"/"+env.getConfiguration()+"-"+env.getSdk());
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
        if (!StringUtils.endsWithIgnoreCase(appName, env.getName().toUpperCase())) {
            appName = appName+" "+env.getName().toUpperCase();
        }
        File ipaFile = new File(new File(getProject().getBuildDir(), "ipadist"), appName+".ipa");
        ZipUtil.compressDirectory(new File(buildDir, "Payload"), true, ipaFile);
        FileUtils.deleteDirectory(payloadDir);
        //
        List<File> dsyms = CommonUtil.findIosDsymsinDirectory(buildDir);
        if (dsyms.size() == 1 ) {
            File dsymDir = dsyms.get(0);
            ZipUtil.compressDirectory(dsymDir, true, new File(new File(getProject().getBuildDir(), "dsymdist"), "dSYM."+env.getName()+".zip"));
            FileUtils.deleteDirectory(dsymDir);
        } else {
            logger.warn("None or multiple DSYMs found! Not moving to distribution folder.");
        }
    }

}
