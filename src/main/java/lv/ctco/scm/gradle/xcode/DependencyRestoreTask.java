/*
 * @(#)DependencyRestoreTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.gradle.xdeps.XdepsUtil;
import lv.ctco.scm.mobile.utils.CarthageUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
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

    @TaskAction
    public void doTaskAction() {
        File cartfile = new File(getProject().getProjectDir(), "Cartfile");
        File cartfileResolved = new File(getProject().getProjectDir(), "Cartfile.resolved");
        if (cartfile.exists() || cartfileResolved.exists()) {
            logger.lifecycle("Restoring Carthage dependencies...");
            if (cartfileResolved.exists()) {
                bootstrapCarthage(cartfileResolved);
            } else {
                updateCarthage(cartfile);
            }
            logger.lifecycle("Restoring Carthage dependencies - done.");
        }
        //
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
    }

    private void bootstrapCarthage(File cartfileResolved) {
        ExecResult bootstrap = CarthageUtil.bootstrap(cartfileResolved);
        if (!bootstrap.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), "Failed to bootstrap Carthage dependencies");
        }
    }

    private void updateCarthage(File cartfile) {
        ExecResult update = CarthageUtil.update(cartfile);
        if (!update.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), "Failed to update Carthage dependencies");
        }
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
