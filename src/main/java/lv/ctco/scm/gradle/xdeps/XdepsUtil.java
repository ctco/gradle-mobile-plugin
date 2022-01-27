/*
 * @(#)XdepsUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
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
        Configuration xdepsConfiguration = project.getConfigurations().findByName(XdepsPlugin.XDEPS_CONFIGURATION_NAME);
        return xdepsConfiguration == null ? Collections.emptySet() : xdepsConfiguration.getFiles();
    }

    public static void checkXdepsConfiguration(XdepsConfiguration xdeps) throws IOException {
        if (StringUtils.isEmpty(xdeps.getGroupId()) || StringUtils.isEmpty(xdeps.getVersion())) {
            throw new IOException("Mandatory properties xdeps.groupId and/or xdeps.version have not been set");
        }
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
        PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
        PublicationContainer publicationContainer = publishingExtension.getPublications();
        for (Publication publication : publicationContainer) {
            xdeps.add((DefaultMavenPublication)publication);
        }
        return xdeps;
    }

    public static boolean hasMavenPublications(Project project) {
        return !getMavenPublications(project).isEmpty();
    }

    public static List<DefaultMavenArtifactRepository> getMavenRepositories(Project project) {
        List<DefaultMavenArtifactRepository> repos = new ArrayList<>();
        PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);
        RepositoryHandler repositoryHandler = publishingExtension.getRepositories();
        for (ArtifactRepository repo : repositoryHandler) {
            repos.add((DefaultMavenArtifactRepository)repo);
        }
        return repos;
    }

    public static boolean hasMavenRepository(Project project, String repoName) {
        for (DefaultMavenArtifactRepository repo : getMavenRepositories(project)) {
            if (repoName.equals(repo.getName())) {
                return true;
            }
        }
        return false;
    }

}
