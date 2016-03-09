/*
 * @(#)CleanTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class CleanTask extends DefaultTask {

    private File solutionFile;

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            BackupUtil.restoreAllFiles();
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
        try {
            // TODO : Get all actually existing configurations, run clean command for all
            cleanConfiguration("Debug");
            cleanConfiguration("Release");
            cleanConfiguration("Ad-Hoc");
            cleanConfiguration("AppStore");
            cleanConfiguration("UITests");
        } catch (IOException ignore) {
            // Ignoring expected exceptions
            LoggerUtil.debug(ignore.getMessage());
        }
    }

    private void cleanConfiguration(String configuration) throws IOException {
        CommandLine commandLine = new CommandLine("xbuild");
        commandLine.addArgument("/t:Clean");
        commandLine.addArgument("/p:Configuration="+configuration);
        commandLine.addArgument(solutionFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            LoggerUtil.errorInTask(this.getName(), execResult.getException().getMessage());
            throw new IOException("Clean for configuration "+configuration+" failed");
        }
    }

}
