/*
 * @(#)UnitTestingTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.ExecResult;
import lv.ctco.scm.mobile.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

public class UnitTestingTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(UnitTestingTask.class);

    private String schemeName;

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    @TaskAction
    public void doTaskAction() {
        if (schemeName == null) {
            logger.info("Unit test scheme is not defined. Skipping unit test execution.");
        } else {
            CommandLine commandLine = new CommandLine("xcodebuild");
            commandLine.addArguments(new String[] {"-scheme", schemeName, "-sdk", "iphonesimulator", "clean", "test"}, false);
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
            for (String line : execResult.getOutput()) {
                if (line.contains("** TEST FAILED **")) {
                    ErrorUtil.errorInTask(this.getName(), "Test errors detected, please review logs for details");
                }
            }
        }
    }

}
