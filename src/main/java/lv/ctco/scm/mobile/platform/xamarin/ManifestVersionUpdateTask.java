/*
 * @(#)ManifestVersionUpdateTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
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

    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

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
            String buildVersion;
            if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_SUBS).isEmpty()) {
                buildVersion = "".equals(releaseVersion) ? revision : releaseVersion+"."+revision;
            } else {
                buildVersion = "".equals(releaseVersion) ? revision : releaseVersion+"_"+revision;
            }
            File manifestFile = new File(projectName+"/Properties/AndroidManifest.xml");
            LoggerUtil.info("Setting versionName in manifest file to '"+buildVersion+"'");
            CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionName=)(\".*?\")"), "android:versionName=\""+buildVersion+"\"");
            if (androidVersionCode != null) {
                LoggerUtil.info("Setting versionCode in manifest file to '"+androidVersionCode+"'");
                CommonUtil.replaceInFile(manifestFile, Pattern.compile("(android:versionCode=)(\".*?\")"), "android:versionCode=\""+androidVersionCode+"\"");
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
