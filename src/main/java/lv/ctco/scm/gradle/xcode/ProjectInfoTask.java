/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.PlistUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectInfoTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(this.getClass());

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
        logger.info("{}", xcodeConfiguration);

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

        logger.lifecycle("Project's release version is '{}'", releaseVersion);
        logger.lifecycle("Project's revision is '{}'", revision);
        logger.lifecycle("Project's build version is '{}'", buildVersion);

        if (TeamcityUtil.isTeamcityEnvironment()) {
            TeamcityUtil.setBuildNumber(buildVersion);
            TeamcityUtil.setProjectReleaseVersion(releaseVersion);
        }
    }

}
