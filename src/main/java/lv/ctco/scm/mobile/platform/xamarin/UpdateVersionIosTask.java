/*
 * @(#)UpdateVersionIosTask.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Riga LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.ProfilingUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class UpdateVersionIosTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(UpdateVersionIosTask.class);

    private String projectName;
    private String environmentName;
    private String releaseVersion;
    private boolean updateCFBundleShortVersionString;
    private boolean cleanReleaseVersionForPROD;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setUpdateCFBundleShortVersionString(boolean updateCFBundleShortVersionString) {
        this.updateCFBundleShortVersionString = updateCFBundleShortVersionString;
    }

    public void setCleanReleaseVersionForPROD(boolean cleanReleaseVersionForPROD) {
        this.cleanReleaseVersionForPROD = cleanReleaseVersionForPROD;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            String revision = RevisionUtil.getRevision(getProject());

            File infoPlist = new File(projectName+"/Info.plist");
            logger.info("Read project release version as '{}'", releaseVersion);

            String buildVersion = "".equals(releaseVersion) ? revision : releaseVersion+"."+revision;

            logger.info("Updating '{}'...", infoPlist);
            if (cleanReleaseVersionForPROD && "PROD".equals(environmentName)) {
                PlistUtil.setStringValue(infoPlist, "CFBundleVersion", releaseVersion);
            } else {
                PlistUtil.setStringValue(infoPlist, "CFBundleVersion", buildVersion);
            }
            if (updateCFBundleShortVersionString) {
                PlistUtil.setStringValue(infoPlist, "CFBundleShortVersionString", releaseVersion);
            }
            PlistUtil.validatePlist(infoPlist);
            //
            File rootPlist = new File(projectName+"/Settings.bundle/Root.plist");
            if (rootPlist.exists()) {
                BackupUtil.backupFile(rootPlist);
                logger.info("Searching for dict containing key 'application_version' in '{}'", rootPlist);
                if (cleanReleaseVersionForPROD && "PROD".equals(environmentName)) {
                    ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlist, "application_version", releaseVersion);
                } else {
                    ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlist, "application_version", buildVersion);
                }
                PlistUtil.validatePlist(rootPlist);
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
