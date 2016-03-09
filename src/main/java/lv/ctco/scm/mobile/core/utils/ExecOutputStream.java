/*
 * @(#)ExecResult.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.LogOutputStream;

import java.util.ArrayList;
import java.util.List;

public class ExecOutputStream extends LogOutputStream {

    private List<String> output = new ArrayList<>();

    private boolean routeToOutput;
    private boolean routeToStdout;

    public ExecOutputStream(boolean routeToOutput, boolean routeToStdout) {
        this.routeToOutput = routeToOutput;
        this.routeToStdout = routeToStdout;
    }

    public List<String> getOutput() {
        return output;
    }

    @Override
    protected void processLine(String line, int logLevel) {
        if (routeToOutput) {
            output.add(line);
        }
        if (routeToStdout) {
            LoggerUtil.info(line);
        }
    }

}
