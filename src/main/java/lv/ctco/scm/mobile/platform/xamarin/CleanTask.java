/*
 * @(#)CleanTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
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
            ErrorUtil.errorInTask(this.getName(), e);
        }
        cleanConfiguration("Debug");
        cleanConfiguration("Release");
        cleanConfiguration("Ad-Hoc");
        cleanConfiguration("AppStore");
        cleanConfiguration("UITests");
    }

    private void cleanConfiguration(String configuration) {
        CommandLine commandLine = new CommandLine("xbuild");
        commandLine.addArgument("/t:Clean");
        commandLine.addArgument("/p:XcodeConfiguration="+configuration);
        commandLine.addArgument(solutionFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

}
