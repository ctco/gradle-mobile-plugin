/*
 * @(#)GitUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.git;

import lv.ctco.scm.mobile.utils.ExecResult;
import lv.ctco.scm.mobile.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

public final class GitUtil {

    private static final int HASH_LENGTH_FULL = 40;
    private static final int HASH_LENGTH_SHORT = 7;
    
    private GitUtil() {}

    public static boolean isGitDir(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"rev-parse", "--is-inside-work-tree"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        return execResult.isSuccess();
    }

    public static ExecResult fetchAll(File dir) {
        CommandLine command = new CommandLine("git");
        command.addArguments(new String[]{"fetch", "--all", "--verbose"}, false);
        return ExecUtil.execCommand(command, dir, null, true, false);
    }

    public static long getCheckedoutCommitTimestamp(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"log", "-n", "1", "--pretty=format:%ct"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        if (execResult.isSuccess()) {
            return Long.parseLong(execResult.getOutput().get(0));
        } else {
            return 0L;
        }
    }

    public static String getShortHash(String gitHash) {
        return gitHash.length() == HASH_LENGTH_FULL ? StringUtils.substring(gitHash, 0, HASH_LENGTH_SHORT) : gitHash;
    }

    public static long getCheckedoutCommitNumber(File dir, List<String> excludes) {
        long timestamp = getCheckedoutCommitTimestamp(dir);
        CommandLine command = new CommandLine("git");
        command.addArgument("rev-list");
        command.addArgument("--count");
        for (String exclude : excludes) {
            command.addArgument("--exclude="+exclude, false);
        }
        command.addArgument("--remotes");
        command.addArgument("--before="+timestamp);
        ExecResult execResult = ExecUtil.execCommand(command, dir, null, true, false);
        if (execResult.isSuccess()) {
            return Long.parseLong(execResult.getOutput().get(0));
        } else {
            return 0L;
        }
    }

}
