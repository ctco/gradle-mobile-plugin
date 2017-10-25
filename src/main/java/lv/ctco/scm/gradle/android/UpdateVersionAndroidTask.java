/*
 * @(#)UpdateVersionAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.android;

import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class UpdateVersionAndroidTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(UpdateVersionAndroidTask.class);

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
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private void updateVersion(File manifestFile) throws IOException {
        logger.info("Setting versionName in '"+ manifestFile.getPath()+"' to '"+environment.getBuildVersion()+"'");
        CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionName=)(\".*?\")"), "android:versionName=\""+environment.getBuildVersion()+"\"");
    }

    /*
    private void updateVersionCode(File manifestFile) throws IOException {
        logger.info("Setting versionCode in '"+ manifestFile.toString()+"' to '"+androidVersionCode+"'");
        CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionCode=)(\".*?\")"), "android:versionCode=\""+androidVersionCode+"\"");
    }
    */

}
