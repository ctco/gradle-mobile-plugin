/*
 * @(#)XdepsUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownRepositoryException;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class XdepsUtil {

    private static final String MAVEN_PUBLISH_PLUGIN_ID = "maven-publish";

    private XdepsUtil() {}

    public static Set<File> getXdepsDependencyFiles(Project project) {
        Configuration xdepsConfiguration = project.getConfigurations().findByName("xdeps");
        return xdepsConfiguration == null ? Collections.<File>emptySet() : xdepsConfiguration.getFiles();
    }

    public static boolean isValidXdepsConfiguration(XdepsConfiguration xdeps) {
        return !StringUtils.isEmpty(xdeps.getGroupId()) && !StringUtils.isEmpty(xdeps.getVersion());
    }

    private static boolean isMavenPublishPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(MAVEN_PUBLISH_PLUGIN_ID);
    }

    public static void applyMavenPublishPlugin(Project project) {
        if (isMavenPublishPluginApplied(project)) {
            return;
        }
        project.getPluginManager().apply(MAVEN_PUBLISH_PLUGIN_ID);
    }

    public static void applyXdepsPublishRules(Project project) {
        project.getPluginManager().apply(XdepsPublishRules.class);
    }

    public static List<DefaultMavenPublication> getMavenPublications(Project project) {
        List<DefaultMavenPublication> xdeps = new ArrayList<>();
        PublishingExtension publishingExtension = getPublishingExtension(project);
        if (publishingExtension != null) {
            PublicationContainer publicationContainer = publishingExtension.getPublications();
            for (Publication publication : publicationContainer) {
                xdeps.add((DefaultMavenPublication)publication);
            }
        }
        return xdeps;
    }

    public static List<DefaultMavenArtifactRepository> getMavenRepositories(Project project) {
        List<DefaultMavenArtifactRepository> repos = new ArrayList<>();
        PublishingExtension publishingExtension = getPublishingExtension(project);
        if (publishingExtension != null) {
            RepositoryHandler repositoryHandler = publishingExtension.getRepositories();
            for (ArtifactRepository repo : repositoryHandler) {
                repos.add((DefaultMavenArtifactRepository)repo);
            }
        }
        return repos;
    }

    public static void enforceRequiredMavenRepository(Project project, String repoName) throws IOException {
        if (getMavenRepositories(project).isEmpty()) {
            return;
        }
        RepositoryHandler repositoryHandler = getPublishingExtension(project).getRepositories();
        try {
            DefaultMavenArtifactRepository repo = (DefaultMavenArtifactRepository)repositoryHandler.getByName(repoName);
            repositoryHandler.clear();
            repositoryHandler.add(repo);
        } catch (UnknownRepositoryException e) {
            throw new IOException("Repository with name '"+repoName+"' required for Xdeps publishing was not found", e);
        }
    }

    public static void enforceRequiredXdepsConfiguration(Project project, XdepsConfiguration xdepsConfiguration) {
        List<DefaultMavenPublication> publications = getMavenPublications(project);
        for (DefaultMavenPublication publication : publications) {
            publication.setGroupId(xdepsConfiguration.getGroupId());
            publication.setVersion(xdepsConfiguration.getVersion());
        }
    }

    public static String getRequiredMavenRepositoryName(String version) {
        return version.toUpperCase().endsWith("-SNAPSHOT") ? "MavenMobileSnapshots" : "MavenMobileReleases";
    }

    private static PublishingExtension getPublishingExtension(Project project) {
        return project.getExtensions().getByType(PublishingExtension.class);
    }

}
