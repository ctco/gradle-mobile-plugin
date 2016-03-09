/*
 * @(#)UnitTestingTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class UnitTestingTask extends DefaultTask {

    private static final String ERR_TEST_ERRORS = "Test errors detected, please review logs for details";

    /* Name of the scheme to use for unit testing. */
    private String schemeName;

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    @TaskAction
    public void doTaskAction() {
        if (schemeName == null) {
            LoggerUtil.info("Unit test scheme is not defined. Skipping unit test execution...");
        } else {
            CommandLine commandLine = new CommandLine("xcodebuild");
            commandLine.addArguments(new String[] {"-scheme", schemeName, "-sdk", "iphonesimulator", "clean", "test"}, false);
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
            for (String line : execResult.getOutput()) {
                if (line.contains("** TEST FAILED **")) {
                    LoggerUtil.errorInTask(this.getName(), ERR_TEST_ERRORS);
                    throw new GradleException(ERR_TEST_ERRORS);
                }
            }
        }
    }

}
