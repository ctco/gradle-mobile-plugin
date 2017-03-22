/*
 * @(#)RevisionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;

public final class RevisionUtil {

    private static final Logger logger = Logging.getLogger(RevisionUtil.class);

    private static String revision = null;

    private static final String PROP_VCS_REVISION = "revision";
    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private RevisionUtil() {}

    public static String getRevision(Project project) throws IOException {
        if (StringUtils.isBlank(revision)) {
            if (PropertyUtil.hasProjectProperty(project, PROP_VCS_REVISION) && !StringUtils.isBlank(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION))) {
                setRevision(GitUtil.getShortHash(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION)));
                logger.info("Revision '{}' was set from passed property", revision);
            } else {
                setRevision(getRevisionFromProjectDir(project, PathUtil.getProjectDir()));
                logger.info("Revision '{}' was auto-detected", revision);
            }
        }
        return revision;
    }

    private static void setRevision(String value) {
        revision = value;
    }

    private static String getRevisionFromProjectDir(Project project, File projectDir) throws IOException {
        String result;
        if (GitUtil.isGitDir(projectDir)) {
            if (PropertyUtil.hasProjectProperty(project, PROP_VCS_ROOT_DIR) && !PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_DIR).isEmpty()) {
                if (PropertyUtil.hasProjectProperty(project, PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_SUBS).isEmpty()) {
                    File vcsRootDir = new File(PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_DIR));
                    result = "";
                    for (String subDirName : PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_SUBS).split(",")) {
                        File subDir = new File(vcsRootDir, subDirName);
                        long commitNumber = GitUtil.getCheckedoutCommitNumber(subDir);
                        result = "".equals(result) ? result+commitNumber : result+"."+commitNumber;
                    }
                } else {
                    File commitDir = GitUtil.getSubdirWithLatestCommit(new File(PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_DIR)));
                    result = Long.toString(GitUtil.getCheckedoutCommitNumber(commitDir));
                }
            } else {
                result = Long.toString(GitUtil.getCheckedoutCommitNumber(GitUtil.getGitProjectRoot(projectDir)));
            }
        } else {
            String error = "Failed to detect project's version control system, please pass revision as a property";
            throw new IOException(error);
        }
        return result;
    }

}
