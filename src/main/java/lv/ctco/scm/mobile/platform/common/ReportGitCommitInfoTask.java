/*
 * @(#)ReportGitCommitInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

import lv.ctco.scm.utils.git.GitUtil;

import org.apache.commons.exec.CommandLine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ReportGitCommitInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ReportGitCommitInfoTask.class);

    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    @TaskAction
    public void doTaskAction() throws IOException {

        if (getProject().equals(getProject().getRootProject())) {
            File vcsRootDir = getVcsRootDir();
            List<File> vcsRootSubs = getVcsRootSubs(vcsRootDir);

        } else {
            logger.info("Skipping task as it is not root project's task");
        }

        if (GitUtil.isGitDir(getProject().getProjectDir())) {
            List<String> commitInfo;
            if (getProject().hasProperty(PROP_VCS_ROOT_DIR) && !getProjectProperty(PROP_VCS_ROOT_DIR).isEmpty()) {
                File commitDir = getSubdirWithLatestCommit(new File(getProjectProperty(PROP_VCS_ROOT_DIR)).getCanonicalFile());
                commitInfo = getCommitInfo(commitDir);
            } else {
                commitInfo = getCommitInfo(PathUtil.getProjectDir());
            }
            if (!commitInfo.isEmpty()) {
                File commitInfoFile = new File(PathUtil.getReportCommitDir(), "commit-info.html");
                Files.deleteIfExists(commitInfoFile.toPath());
                FileUtils.writeStringToFile(commitInfoFile, "<pre>", StandardCharsets.UTF_8, true);
                FileUtils.writeLines(commitInfoFile, commitInfo, true);
                FileUtils.writeStringToFile(commitInfoFile, "</pre>", StandardCharsets.UTF_8, true);
            }
        }
    }

    private File getVcsRootDir() {
        if (getProject().hasProperty(PROP_VCS_ROOT_DIR)) {
            return new File(getProject().getProjectDir(), getProjectProperty(PROP_VCS_ROOT_DIR)).getAbsoluteFile();
        } else {
            return getProject().getProjectDir();
        }
    }

    private List<File> getVcsRootSubs(File vcsRootDir) {
        List<File> subs = new ArrayList<>();
        if (getProject().hasProperty(PROP_VCS_ROOT_SUBS)) {
            for (String subDir : getProjectProperty(PROP_VCS_ROOT_SUBS).split(",")) {
                subs.add(new File(vcsRootDir, subDir));
            }
        } else {
            subs.add(vcsRootDir);
        }
        return subs;
    }

    private File getSubdirWithLatestCommit(File rootDir) throws IOException {
        File resultDir = rootDir;
        long latestTimestamp = GitUtil.getCheckedoutCommitTimestamp(rootDir);
        File [] dirs = rootDir.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        if (dirs != null) {
            for (File dir : dirs) {
                logger.debug("[!!!] checking timestamp for '{}'", dir);
                long dirTimestamp = GitUtil.getCheckedoutCommitTimestamp(dir);
                if (dirTimestamp > latestTimestamp) {
                    resultDir = dir;
                    latestTimestamp = dirTimestamp;
                }
            }
        }
        return resultDir;
    }

    private List<String> getCommitInfo(File dir) {
        List<String> commitInfo = new ArrayList<>();
        if (GitUtil.isGitDir(dir)) {
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

    private String getProjectProperty(String propertyName) {
        return getProject().getProperties().get(propertyName).toString();
    }

}
