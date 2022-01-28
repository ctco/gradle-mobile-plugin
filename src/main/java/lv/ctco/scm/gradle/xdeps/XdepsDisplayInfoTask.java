/*
 * @(#)XdepsDisplayInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.utils.TeamcityUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;
import lv.ctco.scm.mobile.utils.VersionUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.IOException;
import java.util.List;

public class XdepsDisplayInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(XdepsDisplayInfoTask.class);

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

        logger.lifecycle("Project's release version is '{}'", releaseVersion);
        logger.lifecycle("Project's build version is '{}'", buildVersion);

        if (TeamcityUtil.isTeamcityEnvironment()) {
            TeamcityUtil.setBuildNumber(buildVersion);
            TeamcityUtil.setProjectReleaseVersion(releaseVersion);
            TeamcityUtil.setProjectXdepsReleaseVersion(releaseVersion);
            TeamcityUtil.setProjectXdepsBuildVersion(buildVersion);
        }

        List<DefaultMavenArtifactRepository> repositories = XdepsUtil.getMavenRepositories(getProject());
        logger.info("Detected {} defined Maven publication repositories", repositories.size());
        for (DefaultMavenArtifactRepository repository : repositories) {
            logger.info(" - '"+repository.getName()+"' "+repository.getUrl());
        }
        List<DefaultMavenPublication> publications = XdepsUtil.getMavenPublications(getProject());
        logger.info("Detected {} defined Maven publication definitions", publications.size());
        for (DefaultMavenPublication publication : publications) {
            logger.info(" - '"+publication.getName()+"' '"+publication.getCoordinates()+"'");
        }
    }

}
