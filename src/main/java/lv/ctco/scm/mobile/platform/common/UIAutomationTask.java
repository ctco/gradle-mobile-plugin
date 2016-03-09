/*
 * @(#)UIAutomationTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.IosSimulatorUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This task executes UIAutomation template for the instruments utility of Xcode . It allows running UI tests from the
 * command line. The only required parameters are appPath and jsPath. Only a single java script execution is
 * supported for the time being so tests have to be grouped up in test suites.
 *
 * The task detects errors based on log messages produced by instruments because the error code does not allow
 * to figure out if there were test failures or not.
 */
public class UIAutomationTask extends DefaultTask {

    private static final String AUTOMATION_TEMPLATE_PATH =
            "/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate";

    /* Name of the executed test. */
    private String testName;

    /** Name of app file to test. Provided or read from solution file. */
    private String appName;

    /** Path to pre-built Xcode application (*.app directory) that has to be tested. */
    private File appPath;

    /** Path to javascript test scenario to run. */
    private File jsPath;

    /** Name of simulator device or name/identifier of hardware device for instruments to execute upon. */
    private String targetDevice;

    /**
     * Location of output result files. By default it points to the script's current directory. When no
     * directory name is given it will create instrumentscli0.trace subdirectory.
     */
    private File resultsPath = new File(".");

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppPath(File appPath) {
        this.appPath = appPath;
    }

    public void setJsPath(File jsPath) {
        this.jsPath = jsPath;
    }

    public void setTargetDevice(String targetDevice) {
        this.targetDevice = targetDevice;
    }

    public void setResultsPath(File resultsPath) {
        this.resultsPath = resultsPath;
    }

    @TaskAction
    public void runTests() {
        try {
            validateParameters();
            // TODO : Fix Simulator controls
            //resetSimulator(appName);

            String device = null;
            if (targetDevice != null) {
                device = IosSimulatorUtil.getKnownDevice(targetDevice);
                if (device == null) {
                    LoggerUtil.warn("Provided targetDevice not found in known device list. Proceeding with default.");
                } else {
                    LoggerUtil.info("Found provided targetDevice as " + device);
                }
            }

            File outputFile = new File(testName + "_UITest.output.txt");
            if (!outputFile.exists()) {
                FileUtils.touch(outputFile);
            }

            CommandLine commandLine = new CommandLine("instruments");
            commandLine.addArgument("-t");
            commandLine.addArgument(AUTOMATION_TEMPLATE_PATH, false);
            if (device != null) {
                commandLine.addArgument("-w");
                commandLine.addArgument(device, false);
            }
            commandLine.addArgument(appPath.getAbsolutePath(), false);
            commandLine.addArgument("-e");
            commandLine.addArgument("UIASCRIPT");
            commandLine.addArgument(jsPath.getAbsolutePath(), false);
            commandLine.addArgument("-e");
            commandLine.addArgument("UIARESULTSPATH");
            commandLine.addArgument(resultsPath.getAbsolutePath(), false);

            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);

            /*
            String line;
            File traceDir;
            if (line.startsWith("Instruments Trace Complete")) {
                traceDir = new File(line.substring(line.indexOf("Output : ")+9, line.length() - 1));
            }
            //ZipUtil.compressDirectory(traceDir, true, new File(traceDir.getParent(), testName+"_UITest.trace.zip"));
            */

            for (String line : generateSummary(execResult.getOutput())) {
                LoggerUtil.info(line);
            }
            if (containsErrors(execResult.getOutput()) || !execResult.isSuccess()) {
                throw new GradleException("Test errors detected, please review logs for details.");
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void validateParameters() throws IOException {
        if (appPath == null) {
            throw new IOException("appPath parameter is not defined");
        }
        if (jsPath == null) {
            throw new IOException("jsPath parameter is not defined");
        }

        if (!appPath.exists()) {
            throw new IOException(appPath+" directory does not exist");
        }
        if (!jsPath.exists()) {
            throw new IOException(jsPath+" file does not exist");
        }

        if (resultsPath != null && !resultsPath.exists()) {
            if (resultsPath.mkdirs()) {
                LoggerUtil.info("Created resultsPath directory "+resultsPath.getAbsolutePath());
            } else {
                throw new IOException("Failed to create resultsPath at "+resultsPath.getAbsolutePath());
            }
        }
    }

    private static void resetSimulator(String appName) throws IOException {
        IosSimulatorUtil.quitDefaultSimulator();
        IosSimulatorUtil.deleteApp(appName);
        IosSimulatorUtil.activateDefaultSimulator();
    }

    private static boolean containsErrors(List<String> commandOutput) {
        boolean result = false;
        for (String line : commandOutput) {
            Pattern errors = Pattern.compile("Instruments Usage Error|Instruments Trace Error|^\\d+-\\d+-\\d+ \\d+:\\d+:\\d+ [-+]\\d+ (Fail:|Error:|None: Script threw an uncaught JavaScript error)");
            Matcher matcher = errors.matcher(line);
            if (matcher.matches()) {
                result = true;
            }
        }
        return result;
    }

    private static List<String> generateSummary(List<String> commandOutput) {
        List<String> testSummary = new ArrayList<>();
        int testsTotal = 0;
        int testsPass = 0;
        int testsFail = 0;
        testSummary.add("======== Testing Summary ========");
        for (String line : commandOutput) {
            if (line.contains("Pass:")) {
                testSummary.add(line);
                testsTotal++;
                testsPass++;
            }
            if (line.contains("Fail:")) {
                testSummary.add(line);
                testsTotal++;
                testsFail++;
            }
        }
        testSummary.add("Executed a total of "+testsTotal+" test(s) - "+testsPass+" passed and "+testsFail+" failed");
        testSummary.add("=================================");
        return testSummary;
    }

}
