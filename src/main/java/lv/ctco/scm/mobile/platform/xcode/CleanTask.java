/*
 * @(#)CleanTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class CleanTask extends DefaultTask {

    @TaskAction
    public void doTaskAction() {
        try {
            BackupUtil.restoreAllFiles();
            FileUtils.deleteDirectory(PathUtil.getBackupDir());
            FileUtils.deleteDirectory(PathUtil.getXcodeDstDir());
            FileUtils.deleteDirectory(PathUtil.getXcodeObjDir());
            FileUtils.deleteDirectory(PathUtil.getXcodeSymDir());
            FileUtils.deleteDirectory(PathUtil.getXcodeSharedDir());
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
