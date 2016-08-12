/*
 * @(#)UpdateVersionAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

public class UpdateVersionAndroidTask extends DefaultTask {

    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            updateVersion(new File(environment.getManifestPath(), "AndroidManifest.xml"));
            if ("aar".equals(environment.getAssemblyType())) {
                updateVersion(new File(environment.getManifestPath(), "aapt/AndroidManifest.xml"));
            }
            // if (androidVersionCode != null) {}
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void updateVersion(File manifestFile) throws IOException {
        LoggerUtil.info("Setting versionName in '"+ manifestFile.toString()+"' to '"+environment.getBuildVersion()+"'");
        CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionName=)(\".*?\")"), "android:versionName=\""+environment.getBuildVersion()+"\"");
    }

    /*
    private void updateVersionCode(File manifestFile) throws IOException {
        LoggerUtil.info("Setting versionCode in '"+ manifestFile.toString()+"' to '"+androidVersionCode+"'");
        CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionCode=)(\".*?\")"), "android:versionCode=\""+androidVersionCode+"\"");
    }
    */

}
