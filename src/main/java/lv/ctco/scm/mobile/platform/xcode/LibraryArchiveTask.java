/*
 * @(#)LibraryArchiveTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.ZipUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LibraryArchiveTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(LibraryArchiveTask.class);

    private static final String ERR_NO_PUBLICATIONS = "No library publication definitions have been found";

    @TaskAction
    public void doTaskAction() {
        List<DefaultMavenPublication> libraries = LibraryUtil.getLibrariesPublications(getProject());
        if (libraries.isEmpty()) {
            ErrorUtil.errorInTask(this.getName(), ERR_NO_PUBLICATIONS);
        } else {
            try {
                File artifactRootDir = PathUtil.getPublicationArtifactDir();
                for (DefaultMavenPublication artifact : libraries) {
                    logger.info("Found 'library' publication configuration");
                    createArtifact(artifactRootDir, artifact.getArtifactId());
                }
            } catch (IOException e) {
                ErrorUtil.errorInTask(this.getName(), e);
            }
        }
    }

    private static void createArtifact(File artifactRootDir, String artifactId) throws IOException {
        File artifactDir = new File(artifactRootDir, artifactId);
        File artifactFile = new File(artifactRootDir, artifactId+".zip");
        if (artifactDir.exists()) {
            logger.info("Archiving artifact folder '"+artifactDir.getAbsolutePath()+"'");
            FileUtils.deleteQuietly(artifactFile);
            ZipUtil.compressDirectory(artifactDir, true, artifactFile);
        } else if (artifactFile.exists()) {
            logger.warn("Missing defined artifact folder but located a ready artifact file!");
        } else {
            throw new IOException("Missing defined artifact folder and/or artifact file "+artifactDir.getAbsolutePath());
        }
    }

}
