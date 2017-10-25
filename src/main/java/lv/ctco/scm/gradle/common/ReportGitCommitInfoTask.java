/*
 * @(#)ReportGitCommitInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.common;

import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;
import lv.ctco.scm.utils.git.GitUtil;

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
import java.util.Collections;
import java.util.List;

public class ReportGitCommitInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ReportGitCommitInfoTask.class);

    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";

    @TaskAction
    public void doTaskAction() {
        if (getProject().equals(getProject().getRootProject())) {
            List<String> commitInfo;
            if (getProject().hasProperty(PROP_VCS_ROOT_DIR) && !getProjectProperty(PROP_VCS_ROOT_DIR).isEmpty()) {
                File commitDir = getSubdirWithLatestCommit(new File(getProjectProperty(PROP_VCS_ROOT_DIR)).getAbsoluteFile());
                commitInfo = getCommitInfo(commitDir);
            } else {
                commitInfo = getCommitInfo(getProject().getProjectDir());
            }
            if (!commitInfo.isEmpty()) {
                try {
                    writeCommitInfo(commitInfo, new File( "build/reports/commit/commit-info.html"));
                } catch (IOException e) {
                    ErrorUtil.errorInTask(this.getName(), e);
                }
            }
        } else {
            logger.info("Skipping task as it is not root project's task");
        }
    }

    private void writeCommitInfo(List<String> commitInfo, File commitInfoFile) throws IOException {
        Files.deleteIfExists(commitInfoFile.toPath());
        FileUtils.forceMkdirParent(commitInfoFile);
        FileUtils.writeStringToFile(commitInfoFile, "<pre>", StandardCharsets.UTF_8, true);
        FileUtils.writeLines(commitInfoFile, commitInfo, true);
        FileUtils.writeStringToFile(commitInfoFile, "</pre>", StandardCharsets.UTF_8, true);
    }

    private File getSubdirWithLatestCommit(File rootDir) {
        File resultDir = rootDir;
        long latestTimestamp = GitUtil.getCheckedoutCommitTimestamp(rootDir);
        File [] dirs = rootDir.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        if (dirs != null) {
            for (File dir : dirs) {
                logger.debug("  checking timestamp for '{}'", dir);
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
        if (GitUtil.isGitDir(dir)) {
            ExecCommand command = new ExecCommand("git");
            command.addArguments(new String[]{"--no-pager", "log", "-1", "--stat", "--stat-width=70", "--stat-count=100"});
            ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
            if (execResult.isSuccess()) {
                return execResult.getOutput();
            }
        }
        return Collections.emptyList();
    }

    private String getProjectProperty(String propertyName) {
        return getProject().getProperties().get(propertyName).toString();
    }

}
