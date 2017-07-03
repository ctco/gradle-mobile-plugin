/*
 * @(#)ExecUtil.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.IOException;

public final class ExecUtil {

    private ExecUtil() {}

    public static ExecResult executeCommand(ExecCommand execCommand, ExecOutputStream execOutputStream) {
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(execCommand.getWorkingDirectory());
        executor.setExitValues(execCommand.getExitValues());
        executor.setStreamHandler(new PumpStreamHandler(execOutputStream));
        try {
            executor.execute(execCommand);
        } catch (IOException e) {
            return new ExecResult(execOutputStream.getOutput(), e);
        }
        return new ExecResult(execOutputStream.getOutput());
    }

    public static ExecResult executeCommandWithPipe(ExecCommand execCommand, ExecOutputStream execOutputStream, String pipeValue) {
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(execCommand.getWorkingDirectory());
        executor.setExitValues(execCommand.getExitValues());
        executor.setStreamHandler(new PumpStreamHandler(execOutputStream, execOutputStream, new ExecInputStream(pipeValue)));
        try {
            executor.execute(execCommand);
        } catch (IOException e) {
            return new ExecResult(execOutputStream.getOutput(), e);
        }
        return new ExecResult(execOutputStream.getOutput());
    }

    public static ExecResult setupSudoCredentials(String password) {
        ExecCommand execCommand = new ExecCommand("sudo");
        execCommand.addArgument("--validate", false);
        execCommand.addArgument("--stdin", false);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(execCommand.getWorkingDirectory());
        executor.setExitValues(execCommand.getExitValues());
        ExecOutputStream execOutputStream = new ExecOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(execOutputStream, execOutputStream, new ExecInputStream(password)));
        try {
            executor.execute(execCommand);
        } catch (IOException e) {
            return new ExecResult(execOutputStream.getOutput(), e);
        }
        return new ExecResult(execOutputStream.getOutput());
    }

    public static ExecResult resetSudoCredentials() {
        ExecCommand execCommand = new ExecCommand("sudo");
        execCommand.addArgument("--remove-timestamp", false);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(execCommand.getWorkingDirectory());
        executor.setExitValues(execCommand.getExitValues());
        ExecOutputStream execOutputStream = new ExecOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(execOutputStream));
        try {
            executor.execute(execCommand);
        } catch (IOException e) {
            return new ExecResult(execOutputStream.getOutput(), e);
        }
        return new ExecResult(execOutputStream.getOutput());
    }

}
