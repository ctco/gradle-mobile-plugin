/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.gradle.utils.AzureDevOpsUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.PlistUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectInfoTask extends DefaultTask {

    private String releaseVersion;
    private String revision;

    private XcodeConfiguration xcodeConfiguration;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public void setXcodeConfiguration(XcodeConfiguration xcodeConfiguration) {
        this.xcodeConfiguration = xcodeConfiguration;
    }

    @TaskAction
    public void doTaskAction() {
        getLogger().info("{}", xcodeConfiguration);

        try {
            revision = RevisionUtil.getRevision(getProject());

            Map<String, String> buildSettings = XcodeUtil.getBuildSettings();
            String productType = XcodeUtil.getBuildSettings().get("PRODUCT_TYPE");
            if ("com.apple.product-type.application".equalsIgnoreCase(productType)) {
                File infoPlist = new File(buildSettings.get("INFOPLIST_FILE"));
                releaseVersion = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }

        String buildVersion = releaseVersion + '.' + revision;

        getLogger().lifecycle("Project's release version is '{}'", releaseVersion);
        getLogger().lifecycle("Project's revision is '{}'", revision);
        getLogger().lifecycle("Project's build version is '{}'", buildVersion);

        if (TeamcityUtil.isTeamcityEnvironment()) {
            getLogger().lifecycle(TeamcityUtil.generateBuildNumberServiceMessage(buildVersion));
            getLogger().lifecycle(TeamcityUtil.generateSetParameterServiceMessage("project.version.iteration", releaseVersion));
        }
        if (AzureDevOpsUtil.isAzureDevOpsEnvironment()) {
            getLogger().lifecycle(AzureDevOpsUtil.generateBuildNumberServiceMessage(buildVersion));
        }
    }

}
