/*
 * @(#)CleanTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.mobile.utils.BackupUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.PathUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
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
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
