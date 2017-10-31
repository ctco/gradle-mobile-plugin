/*
 * @(#)DependencyRestoreTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.gradle.xdeps.XdepsUtil;
import lv.ctco.scm.mobile.utils.NugetUtil;
import lv.ctco.scm.mobile.utils.ZipUtil;
import lv.ctco.scm.utils.exec.ExecResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class DependencyRestoreTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(DependencyRestoreTask.class);

    private File solutionFile;

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    @TaskAction
    public void doTaskAction() {
        Set<File> xdepsFiles = XdepsUtil.getXdepsDependencyFiles(getProject());
        if (!xdepsFiles.isEmpty()) {
            logger.lifecycle("Restoring Xdeps dependencies...");
            File xdepsLibrary = new File(getProject().getProjectDir(), "Libraries");
            prepareXdepsLibrary(xdepsLibrary);
            for (File xdepsFile : xdepsFiles) {
                try {
                    restoreXdepsFileToLibrary(xdepsFile, xdepsLibrary);
                } catch (IOException e) {
                    ErrorUtil.errorInTask(this.getName(), "Failed to restore Xdeps dependencies");
                }
            }
            logger.lifecycle("Restoring Xdeps dependencies - done.");
        }
        //
        logger.lifecycle("Restoring Nuget dependencies...");
        ExecResult restore = NugetUtil.restore(solutionFile);
        if (!restore.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), "Failed to restore Nuget dependencies");
        }
        logger.lifecycle("Restoring Nuget dependencies - done.");
    }

    private void prepareXdepsLibrary(File xdepsLibrary) {
        try {
            FileUtils.forceMkdir(xdepsLibrary);
            FileUtils.cleanDirectory(xdepsLibrary);
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), "Failed to prepare Xdeps Libraries directory");
        }
    }

    private void restoreXdepsFileToLibrary(File xdepsFile, File xdepsLibrary) throws IOException {
        if (FilenameUtils.isExtension(xdepsFile.getName(), "zip")) {
            logger.info("Extracting '{}'", xdepsFile.getName());
            ZipUtil.extractAll(xdepsFile, xdepsLibrary);
        } else {
            logger.info("Copying '{}'", xdepsFile.getName());
            FileUtils.copyFileToDirectory(xdepsFile, xdepsLibrary);
        }
    }

}
