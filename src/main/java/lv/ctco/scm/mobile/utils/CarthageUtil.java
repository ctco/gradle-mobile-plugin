/*
 * @(#)CarthageUtil.java
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

public class CarthageUtil {

    private CarthageUtil() {}

    public static ExecResult update(File cartfile) {
        ExecCommand command = new ExecCommand("carthage");
        String[] commandArgs = new String[]{"update", "--cache-builds", "--platform", "iOS"};
        command.addArguments(commandArgs, false);
        command.setWorkingDirectory(cartfile.getParentFile());
        return ExecUtil.executeCommand(command, new ExecOutputStream(new NullOutputFilter(), new FullOutputFilter()));
    }

    public static ExecResult bootstrap(File cartfileResolved) {
        ExecCommand command = new ExecCommand("carthage");
        String[] commandArgs = new String[]{"bootstrap", "--cache-builds", "--platform", "iOS"};
        command.addArguments(commandArgs, false);
        command.setWorkingDirectory(cartfileResolved.getParentFile());
        return ExecUtil.executeCommand(command, new ExecOutputStream(new NullOutputFilter(), new FullOutputFilter()));
    }

}
