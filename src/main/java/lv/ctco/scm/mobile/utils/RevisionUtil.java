/*
 * @(#)RevisionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.gradle.utils.PropertyUtil;
import lv.ctco.scm.utils.git.GitUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RevisionUtil {

    private static final Logger logger = Logging.getLogger(RevisionUtil.class);

    private static String revision = null;

    private static final String PROP_VCS_REVISION = "revision";

    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private static final String PROP_GIT_REMOTES_FETCH = "git.remotes.fetch";
    private static final String PROP_GIT_REMOTES_EXCLUDE = "git.remotes.exclude";

    private RevisionUtil() {}

    public static String getRevision(Project project) throws IOException {
        if (StringUtils.isBlank(revision)) {
            if (PropertyUtil.hasProjectProperty(project, PROP_VCS_REVISION) && !StringUtils.isBlank(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION))) {
                setRevision(GitUtil.getShortHash(PropertyUtil.getProjectProperty(project, PROP_VCS_REVISION)));
                logger.info("Revision '{}' was set from project property", revision);
            } else {
                setRevision(getRevisionFromProjectDir(project, project.getProjectDir()));
                logger.info("Revision '{}' was auto-detected", revision);
            }
        }
        return revision;
    }

    private static void setRevision(String value) {
        revision = value;
    }

    private static String getRevisionFromProjectDir(Project project, File projectDir) throws IOException {
        logger.debug("Searching for Git repo in {}...", projectDir.getCanonicalFile());
        if (!GitUtil.isGitDir(projectDir)) {
            String error = "Failed to detect project's version control system, please pass revision via '"+PROP_VCS_REVISION+"' property";
            throw new IOException(error);
        }
        String result;
        List<String> excludes = getRemotesExcludes(project);
        if (projectHasVcsOverrides(project)) {
            File vcsRootDir = new File(PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_DIR)).getCanonicalFile();
            result = "";
            for (String subDirName : PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_SUBS).split(",")) {
                File subDir = new File(vcsRootDir, subDirName).getCanonicalFile();
                fetchAllUnlessOverride(project, subDir);
                logger.info("Calculating revision number for '{}' using excludes {}", subDir.getCanonicalFile(), excludes);
                long commitNumber = GitUtil.getCheckedoutCommitNumber(subDir, excludes);
                result = "".equals(result) ? result+commitNumber : result+"."+commitNumber;
            }
        } else {
            fetchAllUnlessOverride(project, projectDir);
            logger.info("Calculating revision number for '{}' using excludes {}", projectDir.getCanonicalFile(), excludes);
            result = Long.toString(GitUtil.getCheckedoutCommitNumber(projectDir, excludes));
        }
        return result;
    }

    private static boolean projectHasVcsOverrides(Project project) {
        return PropertyUtil.hasProjectProperty(project, PROP_VCS_ROOT_DIR) && !PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_DIR).isEmpty()
                && PropertyUtil.hasProjectProperty(project, PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(project, PROP_VCS_ROOT_SUBS).isEmpty();
    }

    private static List<String> getRemotesExcludes(Project project) {
        List<String> excludes = new ArrayList<>();
        for (String key : project.getProperties().keySet()) {
            if (PROP_GIT_REMOTES_EXCLUDE.equals(key) || key.startsWith(PROP_GIT_REMOTES_EXCLUDE+".")) {
                excludes.add(project.getProperties().get(key).toString());
            }
        }
        return excludes;
    }
    
    private static void fetchAllUnlessOverride(Project project, File dir) throws IOException {
        boolean fetchRemotes = true;
        if (PropertyUtil.hasProjectProperty(project, PROP_GIT_REMOTES_FETCH) && !PropertyUtil.getProjectProperty(project, PROP_GIT_REMOTES_FETCH).isEmpty()) {
            fetchRemotes = Boolean.parseBoolean(PropertyUtil.getProjectProperty(project, PROP_GIT_REMOTES_FETCH));
        }
        if (fetchRemotes) {
            logger.info("Trying to fetch objects and refs from all remotes in '{}' for revision calculation...", dir.getCanonicalFile());
            ExecResult result = GitUtil.fetchAll(dir);
            for (String line : result.getOutput()) {
                logger.debug("  {}", line);
            }
            if (result.isSuccess()) {
                logger.info("  Success.");
            } else {
                logger.error("  Failure.");
                throw new IOException("Failed to fetch git remotes");
            }
        } else {
            logger.info("Skipping object and ref fetch because of override parameter '{}'", PROP_GIT_REMOTES_FETCH);
        }
    }

}
