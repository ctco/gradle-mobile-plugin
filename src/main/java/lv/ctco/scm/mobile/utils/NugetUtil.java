/*
 * @(#)NugetUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import java.io.File;

public final class NugetUtil {

    private NugetUtil() {}

    public static ExecResult restore(File projectFile) {
        ExecCommand command = new ExecCommand("nuget");
        command.addArgument("restore");
        command.addArgument(projectFile.getName());
        command.setWorkingDirectory(projectFile.getParentFile());
        return ExecUtil.executeCommand(command, new ExecOutputStream(new NullOutputFilter(), new FullOutputFilter()));
    }

}
