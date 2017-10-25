/*
 * @(#)ExecResult.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import java.util.List;

public class ExecResult {

    private boolean success;
    private List<String> output;
    private Exception exception;

    public ExecResult(boolean success, List<String> output, Exception exception) {
        this.success = success;
        this.output = output;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getOutput() {
        return output;
    }

    public Exception getException() {
        return exception;
    }

}
