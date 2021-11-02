/*
 * @(#)ExecOutputStream.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import org.apache.commons.exec.LogOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecOutputStream extends LogOutputStream {

    private Logger logger;

    private ExecOutputFilter outputFilter;
    private ExecOutputFilter loggerFilter;

    private List<String> output = new ArrayList<>();

    public ExecOutputStream() {
        this.logger = LoggerFactory.getLogger(getClass());
        this.outputFilter = new NullOutputFilter();
        this.loggerFilter = new NullOutputFilter();
    }

    public ExecOutputStream(ExecOutputFilter outputFilter, ExecOutputFilter loggerFilter) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.outputFilter = outputFilter;
        this.loggerFilter = loggerFilter;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ExecOutputFilter getOutputFilter() {
        return outputFilter;
    }

    public void setOutputFilter(ExecOutputFilter outputFilter) {
        this.outputFilter = outputFilter;
    }

    public ExecOutputFilter getLoggerFilter() {
        return loggerFilter;
    }

    public void setLoggerFilter(ExecOutputFilter loggerFilter) {
        this.loggerFilter = loggerFilter;
    }

    public List<String> getOutput() {
        return output;
    }

    @Override
    protected void processLine(String line, int logLevel) {
        if (outputFilter.matchesFilter(line)) {
            output.add(line);
        }
        if (loggerFilter.matchesFilter(line)) {
            logger.info(line);
        }
    }

}
