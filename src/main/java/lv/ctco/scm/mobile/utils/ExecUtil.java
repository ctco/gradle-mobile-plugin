/*
 * @(#)ExecUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.File;
import java.io.IOException;

public final class ExecUtil {

    private ExecUtil() {}

    public static ExecResult execCommand(CommandLine commandLine, File workDir, int[] exitValues, boolean routeToCapture, boolean routeToStdout) {
        Executor executor = new DefaultExecutor();
        if (workDir != null) {
            executor.setWorkingDirectory(workDir);
        }
        if (exitValues != null) {
            executor.setExitValues(exitValues);
        }
        ExecOutputStream execOutputStream = new ExecOutputStream(routeToCapture, routeToStdout);
        PumpStreamHandler streamHandler = new PumpStreamHandler(execOutputStream);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(commandLine);
        } catch (IOException e) {
            return new ExecResult(false, execOutputStream.getOutput(), e);
        }
        return new ExecResult(true, execOutputStream.getOutput(), null);
    }

}
