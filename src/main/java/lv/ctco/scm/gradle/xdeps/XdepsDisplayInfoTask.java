/*
 * @(#)XdepsDisplayInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.utils.AzureDevOpsUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;
import lv.ctco.scm.mobile.utils.VersionUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.IOException;
import java.util.List;

public class XdepsDisplayInfoTask extends DefaultTask {

    private XdepsConfiguration xdepsConfiguration;

    public void setXdepsConfiguration(XdepsConfiguration xdepsConfiguration) {
        this.xdepsConfiguration = xdepsConfiguration;
    }

    @TaskAction
    public void doTaskAction() {
        String releaseVersion = null;
        String buildVersion = null;
        if (getProject().hasProperty("xdeps.version")) {
            buildVersion = getProject().getProperties().get("xdeps.version").toString();
            try {
                releaseVersion = VersionUtil.normalizeToMajorMinorPatchVersion(buildVersion);
            } catch (IOException e) {
                throw new TaskExecutionException(this, e);
            }
        } else {
            releaseVersion = StringUtils.removeEndIgnoreCase(xdepsConfiguration.getVersion(), "-SNAPSHOT");
            try {
                buildVersion = releaseVersion + '.' + RevisionUtil.getRevision(getProject());
            } catch (IOException e) {
                throw new TaskExecutionException(this, e);
            }
        }

        getLogger().lifecycle("Project's release version is '{}'", releaseVersion);
        getLogger().lifecycle("Project's build version is '{}'", buildVersion);

        if (TeamcityUtil.isTeamcityEnvironment()) {
            getLogger().lifecycle(TeamcityUtil.generateBuildNumberServiceMessage(buildVersion));
            getLogger().lifecycle(TeamcityUtil.generateSetParameterServiceMessage("project.version.iteration", releaseVersion));
            getLogger().lifecycle(TeamcityUtil.generateSetParameterServiceMessage("project.xdeps.version.iteration", releaseVersion));
            getLogger().lifecycle(TeamcityUtil.generateSetParameterServiceMessage("project.xdeps.version.publish", buildVersion));
        }
        if (AzureDevOpsUtil.isAzureDevOpsEnvironment()) {
            getLogger().lifecycle(AzureDevOpsUtil.generateBuildNumberServiceMessage(buildVersion));
        }

        List<DefaultMavenArtifactRepository> repositories = XdepsUtil.getMavenRepositories(getProject());
        getLogger().info("Detected {} defined Maven publication repositories", repositories.size());
        for (DefaultMavenArtifactRepository repository : repositories) {
            getLogger().info(" - '"+repository.getName()+"' "+repository.getUrl());
        }
        List<DefaultMavenPublication> publications = XdepsUtil.getMavenPublications(getProject());
        getLogger().info("Detected {} defined Maven publication definitions", publications.size());
        for (DefaultMavenPublication publication : publications) {
            getLogger().info(" - '"+publication.getName()+"' '"+publication.getCoordinates()+"'");
        }
    }

}
