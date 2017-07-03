/*
 * @(#)ExecResult.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import java.io.IOException;
import java.util.List;

public final class ExecResult {

    private boolean success;
    private List<String> output;
    private IOException exception;

    ExecResult(List<String> output) {
        this.output = output;
        this.success = true;
    }

    ExecResult(List<String> output, IOException exception) {
        this.output = output;
        this.exception = exception;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getOutput() {
        return output;
    }

    public IOException getException() {
        return exception;
    }

}
