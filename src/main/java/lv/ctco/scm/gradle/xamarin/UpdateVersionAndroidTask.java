/*
 * @(#)UpdateVersionAndroidTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class UpdateVersionAndroidTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(UpdateVersionAndroidTask.class);

    private String projectName;
    private String releaseVersion;
    private String androidVersionCode;

    public void setProjectFile(File projectFile) {
        this.projectName = projectFile.getParentFile().getName();
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setAndroidVersionCode(String androidVersionCode) {
        this.androidVersionCode = androidVersionCode;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            String revision = RevisionUtil.getRevision(getProject());
            String buildVersion = "".equals(releaseVersion) ? revision : releaseVersion+"."+revision;
            File manifestFile = new File(projectName+"/Properties/AndroidManifest.xml");
            logger.info("Setting versionName in manifest file to '{}'", buildVersion);
            CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionName=)(\".*?\")"), "android:versionName=\""+buildVersion+"\"");
            if (androidVersionCode != null) {
                logger.info("Setting versionCode in manifest file to '{}'", androidVersionCode);
                CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionCode=)(\".*?\")"), "android:versionCode=\""+androidVersionCode+"\"");
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
