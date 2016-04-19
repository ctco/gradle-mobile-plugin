/*
 * @(#)GitUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
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

    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";

    private GitUtil() {}

    public static String getShortHash(String gitHash) {
        return gitHash.length() == HASH_SIZE_FULL ? StringUtils.substring(gitHash, 0, HASH_SIZE_SHORT) : gitHash;
    }

    public static boolean isGitDir(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"rev-parse", "--is-inside-work-tree"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        return execResult.isSuccess();
    }

    protected static File getGitProjectRoot(File dir) {
        File rootDir = null;
        if (isGitDir(dir)) {
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

    public static void generateCommitInfo(File commitInfoFile) throws IOException {
        if (isGitDir(PathUtil.getProjectDir())) {
            List<String> commitInfo;
            if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_DIR) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_DIR).isEmpty()) {
                File commitDir = GitUtil.getSubdirWithLatestCommit(new File(PropertyUtil.getProjectProperty(PROP_VCS_ROOT_DIR)));
                commitInfo = getCommitInfo(commitDir);
            } else {
                commitInfo = getCommitInfo(PathUtil.getProjectDir());
            }
            if (!commitInfo.isEmpty()) {
                LoggerUtil.debug("Git commit info:");
                for (String line : commitInfo) {
                    LoggerUtil.debug(line);
                }
                Files.deleteIfExists(commitInfoFile.toPath());
                FileUtils.writeStringToFile(commitInfoFile, "<pre>", true);
                FileUtils.writeLines(commitInfoFile, commitInfo, true);
                FileUtils.writeStringToFile(commitInfoFile, "</pre>", true);
            }
        }
    }

    private static List<String> getCommitInfo(File dir) {
        List<String> commitInfo = new ArrayList<>();
        if (isGitDir(dir)) {
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

    protected static String getCheckedoutCommitHashShort(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"log", "-n", "1", "--pretty=format:%h"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        if (execResult.isSuccess()) {
            return execResult.getOutput().get(0);
        } else {
            return "";
        }
    }

    protected static long getCheckedoutCommitTimestamp(File dir) {
        CommandLine commandLine = new CommandLine("git");
        commandLine.addArguments(new String[] {"log", "-n", "1", "--pretty=format:%ct"}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, dir, null, true, false);
        if (execResult.isSuccess()) {
            return Long.parseLong(execResult.getOutput().get(0));
        } else {
            return 0L;
        }
    }

    protected static File getSubdirWithLatestCommit(File rootDir) {
        File resultDir = rootDir;
        long latestCommitTimestamp = GitUtil.getCheckedoutCommitTimestamp(rootDir);
        for (File rootSubDir : rootDir.listFiles((FileFilter)FileFilterUtils.directoryFileFilter())) {
            long dirCommitTimestamp = GitUtil.getCheckedoutCommitTimestamp(rootSubDir);
            if (dirCommitTimestamp > latestCommitTimestamp) {
                resultDir = rootSubDir;
                latestCommitTimestamp = dirCommitTimestamp;
            }
        }
        return resultDir;
    }

}
