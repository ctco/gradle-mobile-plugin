/*
 * @(#)ExecResult.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.LogOutputStream;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.ArrayList;
import java.util.List;

class ExecOutputStream extends LogOutputStream {

    private static final Logger logger = Logging.getLogger(ExecOutputStream.class);

    private List<String> output = new ArrayList<>();

    private boolean routeToOutput;
    private boolean routeToLogger;

    ExecOutputStream(boolean routeToOutput, boolean routeToLogger) {
        this.routeToOutput = routeToOutput;
        this.routeToLogger = routeToLogger;
    }

    public List<String> getOutput() {
        return output;
    }

    @Override
    protected void processLine(String line, int logLevel) {
        if (routeToOutput) {
            output.add(line);
        }
        if (routeToLogger) {
            logger.info(line);
        }
    }

}
