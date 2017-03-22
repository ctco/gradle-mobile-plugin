/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.StampUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

public class ProjectInfoTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(ProjectInfoTask.class);

    private String releaseVersion;
    private String revision;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @TaskAction
    public void doTaskAction() {
        String buildVersion = releaseVersion+"."+revision;
        logger.lifecycle("Project's release version is '{}'", releaseVersion);
        logger.lifecycle("Project's revision is '{}'", revision);
        logger.lifecycle("Project's build version is '{}'", buildVersion);

        TeamcityUtil.setBuildNumber(buildVersion);
        TeamcityUtil.setAgentParameter("build.number", buildVersion);
        TeamcityUtil.setAgentParameter("project.version.iteration", releaseVersion);
        if (PropertyUtil.hasProjectProperty(getProject(), "stamp")) {
            StampUtil.updateStamp(PropertyUtil.getProjectProperty(getProject(), "stamp"), releaseVersion);
        }
    }

}
