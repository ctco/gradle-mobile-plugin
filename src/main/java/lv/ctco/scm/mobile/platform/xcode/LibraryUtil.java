/*
 * @(#)LibraryUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LibraryUtil {

    private static final String PUBLISHING_EXT_NAME = "publishing";
    private static final String PUBLISHING_PREFIX = "library";

    private LibraryUtil() {}

    public static List<DefaultMavenPublication> getLibrariesPublications(Project project) {
        List<DefaultMavenPublication> libraries = new ArrayList<>();
        PublishingExtension publishingExtension = (PublishingExtension)project.getExtensions().findByName(PUBLISHING_EXT_NAME);
        if (publishingExtension != null) {
            PublicationContainer publicationContainer = publishingExtension.getPublications();
            for (Publication publication : publicationContainer) {
                if (publication.getName().toLowerCase().startsWith(PUBLISHING_PREFIX)) {
                    libraries.add((DefaultMavenPublication)publication);
                }
            }
        }
        return libraries;
    }

    static void printLibrariesPublicationsInfo(Project project) {
        List<DefaultMavenPublication> libraries = getLibrariesPublications(project);
        if (!libraries.isEmpty()) {
            LoggerUtil.info("Detected "+libraries.size()+" defined library publication(s):");
            for (DefaultMavenPublication library : libraries) {
                LoggerUtil.info(" - '"+library.getName()+"' '"+library.getCoordinates()+"'");
            }
        }
    }

    static List<DefaultMavenArtifactRepository> getLibrariesRepositories(Project project) {
        List<DefaultMavenArtifactRepository> repositories = new ArrayList<>();
        PublishingExtension publishingExtension = (PublishingExtension)project.getExtensions().findByName(PUBLISHING_EXT_NAME);
        if (publishingExtension != null) {
            RepositoryHandler repositoryHandler = publishingExtension.getRepositories();
            for (ArtifactRepository repository : repositoryHandler) {
                repositories.add((DefaultMavenArtifactRepository)repository);
            }
        }
        return repositories;
    }

    public static void printLibrariesRepositoriesInfo(Project project) {
        List<DefaultMavenArtifactRepository> repositories = getLibrariesRepositories(project);
        if (!repositories.isEmpty()) {
            LoggerUtil.info("Detected defined library publication repositories:");
            for (ArtifactRepository repository : repositories) {
                LoggerUtil.info(" - '"+repository.getName()+"' "+((DefaultMavenArtifactRepository)repository).getUrl());
            }
        }
    }

    public static void configureSingleMavenLibraryRepository(Project project, String repoName) throws IOException {
        List<DefaultMavenArtifactRepository> repositories = getLibrariesRepositories(project);
        if (!repositories.isEmpty()) {
            PublishingExtension publishingExtension = (PublishingExtension)project.getExtensions().findByName(PUBLISHING_EXT_NAME);
            RepositoryHandler repositoryHandler = publishingExtension.getRepositories();
            DefaultMavenArtifactRepository repository = (DefaultMavenArtifactRepository)repositoryHandler.getByName(repoName);
            if (repository == null) {
                throw new IOException("Required Maven repository '"+repoName+"' configuration not found!");
            } else {
                repositoryHandler.clear();
                repositoryHandler.add(repository);
            }
        }
    }

}
