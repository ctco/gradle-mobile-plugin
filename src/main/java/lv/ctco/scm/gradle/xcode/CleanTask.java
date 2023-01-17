/*
 * @(#)CleanTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.mobile.utils.BackupUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class CleanTask extends DefaultTask {

    @TaskAction
    public void doTaskAction() {
        try {
            BackupUtil.restoreAllFiles();
            FileUtils.deleteDirectory(new File(getProject().getBuildDir(), "backups"));
            FileUtils.deleteDirectory(new File(getProject().getBuildDir(), "xcodebuild/dst"));
            FileUtils.deleteDirectory(new File(getProject().getBuildDir(), "xcodebuild/obj"));
            FileUtils.deleteDirectory(new File(getProject().getBuildDir(), "xcodebuild/sym"));
            FileUtils.deleteDirectory(new File(getProject().getBuildDir(), "xcodebuild/shared"));
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
