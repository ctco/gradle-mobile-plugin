/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import lv.ctco.scm.mobile.core.utils.StampUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class ProjectInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ProjectInfoTask.class);

    private String releaseVersion;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            String revision = RevisionUtil.getRevision(getProject());
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

            GitUtil.generateCommitInfo(getProject());
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
