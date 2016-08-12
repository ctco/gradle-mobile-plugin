/*
 * @(#)RevisionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

public final class RevisionUtil {

    private static String revision = null;

    private static final String PROP_VCS_REVISION = "revision";
    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private RevisionUtil() {}

    public static String getRevision(Project project) throws IOException {
        if (StringUtils.isBlank(revision)) {
            if (PropertyUtil.hasProjectProperty(project, PROP_VCS_REVISION) && !StringUtils.isBlank(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION))) {
                setRevision(GitUtil.getShortHash(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION)));
                LoggerUtil.info("Revision '"+revision+"' was set from passed property");
            } else {
                setRevision(getRevisionFromProjectDir(project, PathUtil.getProjectDir()));
                LoggerUtil.info("Revision '"+revision+"' was auto-detected");
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
            LoggerUtil.info("Git repo detected.");
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
                    result = ""+GitUtil.getCheckedoutCommitNumber(commitDir);
                }
            } else {
                result = ""+GitUtil.getCheckedoutCommitNumber(GitUtil.getGitProjectRoot(projectDir));
            }
        } else {
            String error = "Failed to detect project's version control system, please pass revision as a property";
            LoggerUtil.warn(error);
            throw new IOException(error);
        }
        return result;
    }

}
