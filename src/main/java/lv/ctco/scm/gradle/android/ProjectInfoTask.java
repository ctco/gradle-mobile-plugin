/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.android;

import lv.ctco.scm.gradle.utils.AzureDevOpsUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ProjectInfoTask extends DefaultTask {

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
