/*
 * @(#)UpdateVersionTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.ProfilingUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class UpdateVersionTask extends DefaultTask {

    private String projectName;
    private String environmentName;
    private String releaseVersion;
    private boolean updateCFBundleShortVersionString;
    private boolean cleanReleaseVersionForPROD;
    private boolean enforcePlistSyntax;

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

    public void setEnforcePlistSyntax(boolean enforcePlistSyntax) {
        this.enforcePlistSyntax = enforcePlistSyntax;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            String revision = RevisionUtil.getRevision();
            LoggerUtil.info("Detected revision as '" + revision + "'");

            String plistFileName = projectName+"/Info.plist";
            LoggerUtil.info("Read project release version as '"+releaseVersion+"'");

            String buildVersionToSet = "".equals(releaseVersion) ? revision : releaseVersion+"_"+revision;
            BackupUtil.backupFile(new File(plistFileName));
            if (cleanReleaseVersionForPROD && "PROD".equals(environmentName)) {
                PlistUtil.setStringValue(new File(plistFileName), "CFBundleVersion", releaseVersion);
            } else {
                PlistUtil.setStringValue(new File(plistFileName), "CFBundleVersion", buildVersionToSet);
            }
            if (updateCFBundleShortVersionString) {
                PlistUtil.setStringValue(new File(plistFileName), "CFBundleShortVersionString", releaseVersion);
            }
            if (enforcePlistSyntax) {
                PlistUtil.validatePlist(new File(plistFileName));
            }
            //
            String rootPlistFileName = projectName+"/Settings.bundle/Root.plist";
            File rootPlistFile = new File(rootPlistFileName);
            if (rootPlistFile.exists()) {
                BackupUtil.backupFile(rootPlistFile);
                LoggerUtil.info("Searching for dict containing key 'application_version' in '"+rootPlistFileName+"'");
                if (cleanReleaseVersionForPROD && "PROD".equals(environmentName)) {
                    ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlistFile, "application_version", releaseVersion);
                } else {
                    ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlistFile, "application_version", buildVersionToSet);
                }
                if (enforcePlistSyntax) {
                    PlistUtil.validatePlist(rootPlistFile);
                }
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
