/*
 * @(#)UnitTestingTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class UnitTestingTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(UnitTestingTask.class);

    private String unitTestProject;

    public void setUnitTestProject(String unitTestProject) {
        this.unitTestProject = unitTestProject;
    }

    @TaskAction
    public void doTaskAction() {
        if (unitTestProject == null) {
            logger.info("Unit test project is not defined. Skipping unit test execution.");
        } else {
            try {
                buildProject();
                testProject();
            } catch (IOException e) {
                ErrorUtil.errorInTask(this.getName(), e);
            }
        }
    }

    private void buildProject() {
        File mdtool = new File("/Applications/Xamarin Studio.app/Contents/MacOS/mdtool");
        String buildTool = mdtool.exists() ? "xbuild" : "msbuild";
        CommandLine commandLine = new CommandLine(buildTool);
        commandLine.addArgument("/property:Configuration=Debug");
        commandLine.addArgument("/target:Build");
        commandLine.addArgument(unitTestProject, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

    private void testProject() throws IOException {
        File reportFile = new File(PathUtil.getReportUnitDir(), "UnitTestResult.xml");
        CommandLine commandLine = new CommandLine("nunit-console");
        commandLine.addArgument("-labels");
        commandLine.addArgument("-output="+reportFile.getAbsolutePath(), false);
        commandLine.addArgument(unitTestProject, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

}
