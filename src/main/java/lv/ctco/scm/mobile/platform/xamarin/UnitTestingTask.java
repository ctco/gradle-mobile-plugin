/*
 * @(#)UnitTestingTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class UnitTestingTask extends DefaultTask {

    /* Path to the project file to use for unit testing. */
    private String unitTestProject;

    public void setUnitTestProject(String unitTestProject) {
        this.unitTestProject = unitTestProject;
    }

    @TaskAction
    public void doTaskAction() {
        if (unitTestProject == null) {
            LoggerUtil.info("Unit test project is not defined. Skipping unit test execution...");
        } else {
            try {
                buildProject();
                testProject();
            } catch (IOException e) {
                LoggerUtil.errorInTask(this.getName(), e.getMessage());
                throw new GradleException(e.getMessage(), e);
            }
        }
    }

    private void buildProject() {
        CommandLine commandLine = new CommandLine("xbuild");
        commandLine.addArgument(unitTestProject, false);
        commandLine.addArgument("/p:Configuration=Debug");
        commandLine.addArgument("/t:Build");
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            LoggerUtil.errorInTask(this.getName(), execResult.getException().getMessage());
            throw new GradleException("Artifact build failed");
        }
    }

    private void testProject() throws IOException {
        File reportFile = new File(PathUtil.getReportUnitDir(), "UnitTestResult.xml");
        CommandLine commandLine = new CommandLine("nunit-console");
        commandLine.addArgument("-output="+reportFile.getAbsolutePath(), false);
        commandLine.addArgument(unitTestProject, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            LoggerUtil.errorInTask(this.getName(), execResult.getException().getMessage());
            throw new GradleException("Testing failed");
        }
    }

}
