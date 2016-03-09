/*
 * @(#)ManifestVersionUpdateTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class ManifestVersionUpdateTask extends DefaultTask {

    private String projectName;
    private String releaseVersion;
    private String androidVersionCode;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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
            String revision = RevisionUtil.getRevision();
            LoggerUtil.info("Detected revision as '" + revision + "'");
            LoggerUtil.info("Read project release version as '" + releaseVersion + "'");
            String buildVersionToSet = "".equals(releaseVersion) ? revision : releaseVersion + "_" + revision;
            File manifestFile = new File(projectName + "/Properties/AndroidManifest.xml");
            BackupUtil.backupFile(manifestFile);
            LoggerUtil.info("Setting versionName in manifest file to '" + buildVersionToSet + "'");
            CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionName=)(\".*?\")"), "android:versionName=\"" + buildVersionToSet + "\"");
            if (androidVersionCode != null) {
                LoggerUtil.info("Setting versionCode in manifest file to '" + androidVersionCode + "'");
                CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionCode=)(\".*?\")"), "android:versionCode=\"" + androidVersionCode + "\"");
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
