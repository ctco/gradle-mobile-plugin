/*
 * @(#)GitUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GitUtil {

    private static final int HASH_SIZE_FULL = 40;
    private static final int HASH_SIZE_SHORT = 7;

    private GitUtil() {}

    public static String getShortHash(String gitHash) {
        return gitHash.length() == HASH_SIZE_FULL ? StringUtils.substring(gitHash, 0, HASH_SIZE_SHORT) : gitHash;
    }

    public static boolean isUnderGit(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"rev-parse", "--is-inside-work-tree"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        return execResult.isSuccess();
    }

    protected static File getGitProjectRoot(File dir) {
        File rootDir = null;
        if (isUnderGit(dir)) {
            CommandLine commandLine = new CommandLine("git");
            commandLine.addArguments(new String[] {"rev-parse", "--show-cdup"}, false);
            ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
            if (execResult.isSuccess()) {
                rootDir = new File(dir, StringUtils.chomp(execResult.getOutput().get(0)));
                LoggerUtil.debug("Git root project path: '"+rootDir.getAbsolutePath()+"'");
            }
        }
        return rootDir;
    }

    public static void generateGitPushMessage(File pushInfoFile) throws IOException {
        if (isUnderGit(PathUtil.getProjectDir())) {
            List<String> gitPushInfo = getGitPushInfo(PathUtil.getProjectDir());
            if (!gitPushInfo.isEmpty()) {
                LoggerUtil.debug("Git push info:");
                for (String line : gitPushInfo) {
                    LoggerUtil.debug(line);
                }
                Files.deleteIfExists(pushInfoFile.toPath());
                FileUtils.writeStringToFile(pushInfoFile, "<pre>", true);
                FileUtils.writeLines(pushInfoFile, gitPushInfo, true);
                FileUtils.writeStringToFile(pushInfoFile, "</pre>", true);
            }
        }
    }

    private static List<String> getGitPushInfo(File dir) {
        List<String> commitInfo = new ArrayList<>();
        if (isUnderGit(dir)) {
            CommandLine commandLine = new CommandLine("git");
            String[] args = new String[]{"--no-pager", "log", "-1", "--stat", "--stat-width=70", "--stat-count=100"};
            commandLine.addArguments(args, false);
            ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
            if (execResult.isSuccess()) {
                commitInfo = execResult.getOutput();
            }
        }
        return commitInfo;
    }

    protected static String getGitShortHash(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"log", "--pretty=format:%h", "-n", "1"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        if (execResult.isSuccess()) {
            return execResult.getOutput().get(0);
        } else {
            return "";
        }
    }

}
