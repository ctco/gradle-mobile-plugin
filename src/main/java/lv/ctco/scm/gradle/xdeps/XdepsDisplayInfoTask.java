/*
 * @(#)XdepsInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;
import lv.ctco.scm.mobile.utils.RevisionUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.List;

public class XdepsDisplayInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(XdepsDisplayInfoTask.class);

    private String releaseVersion;
    private String revision;

    private XdepsConfiguration xdepsConfiguration;

    public void setXdepsConfiguration(XdepsConfiguration xdepsConfiguration) {
        this.xdepsConfiguration = xdepsConfiguration;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            revision = RevisionUtil.getRevision(getProject());
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }

        releaseVersion = StringUtils.removeEndIgnoreCase(xdepsConfiguration.getVersion(), "-SNAPSHOT");
        String buildVersion = releaseVersion + '.' + revision;

        logger.lifecycle("Project's release version is '{}'", releaseVersion);
        logger.lifecycle("Project's revision is '{}'", revision);
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
