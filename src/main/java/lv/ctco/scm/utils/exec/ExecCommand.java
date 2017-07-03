/*
 * @(#)ExecCommand.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.util.StringUtils;

import java.io.File;

public final class ExecCommand extends CommandLine {

    private File workingDirectory;
    private int[] exitValues;

    public ExecCommand(String executable) {
        super(executable);
        this.workingDirectory = new File(".");
        this.exitValues = new int[0];
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public int[] getExitValues() {
        return exitValues.clone();
    }

    public void setExitValues(int[] exitValues) {
        this.exitValues = exitValues.clone();
    }

    @Override
    public String toString() {
        String command = StringUtils.toString(toStrings(), " ");
        if (workingDirectory.equals(new File("."))) {
            return "[" + command + "]";
        } else {
            return "[" + command + "] in [" + workingDirectory.getAbsolutePath() + "]";
        }
    }

}
