/*
 * @(#)ExecOutputStream.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExecOutputStream extends LogOutputStream {

    private Logger logger;

    private ExecOutputFilter outputFilter = new NullOutputFilter();
    private ExecOutputFilter loggerFilter = new NullOutputFilter();

    @Deprecated
    private ExecOutputFilter fileFilter = new NullOutputFilter();

    private List<String> output = new ArrayList<>();

    @Deprecated
    private File logFile;

    public ExecOutputStream() {
        this.logger = LoggerFactory.getLogger(getClass());
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

    @Deprecated
    public ExecOutputFilter getFileFilter() {
        return fileFilter;
    }

    @Deprecated
    public void setFileFilter(ExecOutputFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public List<String> getOutput() {
        return output;
    }

    @Deprecated
    public File getLogFile() {
        return logFile;
    }

    @Deprecated
    public void setLogFile(File logFile) throws IOException {
        if (logFile == null) {
            this.logFile = null;
            this.fileFilter = new NullOutputFilter();
        } else {
            FileUtils.touch(logFile);
            this.logFile = logFile;
            this.fileFilter = new FullOutputFilter();
        }
    }

    @Override
    protected void processLine(String line, int logLevel) {
        if (outputFilter.matchesFilter(line)) {
            output.add(line);
        }
        if (loggerFilter.matchesFilter(line)) {
            logger.info(line);
        }
        if (fileFilter.matchesFilter(line)) {
            try {
                FileUtils.writeStringToFile(logFile, line, StandardCharsets.UTF_8, true);
            } catch (IOException e) {
                logger.debug("Exception while writing command output stream to file", e);
            }
        }
    }

}
