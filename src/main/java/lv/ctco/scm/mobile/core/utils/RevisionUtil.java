/*
 * @(#)RevisionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Singleton
public final class RevisionUtil {

    private static String revision = null;

    private static final String PROP_VCS_REVISION = "revision";
    private static final String PROP_VCS_ROOT_DIR = "vcs.root.dir";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private RevisionUtil() {}

    public static String getRevision() throws IOException {
        if (StringUtils.isBlank(revision)) {
            if (PropertyUtil.hasProjectProperty(PROP_VCS_REVISION) && !StringUtils.isBlank(PropertyUtil.getProjectProperty(PROP_VCS_REVISION))) {
                setRevision(GitUtil.getShortHash(PropertyUtil.getProjectProperty(PROP_VCS_REVISION)));
                LoggerUtil.info("Revision '"+revision+"' was set from passed property");
            } else {
                setRevision(getGitOrSvnRevision(PathUtil.getProjectDir()));
                LoggerUtil.info("Revision '"+revision+"' was auto-detected");
            }
        }
        return revision;
    }

    private static void setRevision(String value) {
        revision = value;
    }

    @VisibleForTesting
    private static void setRevisionIfNull(String value) {
        if (revision == null) {
            revision = value;
        }
    }

    private static String getGitOrSvnRevision(File projectDir) throws IOException {
        String result;
        if (GitUtil.isGitDir(projectDir)) {
            LoggerUtil.info("Git repo detected.");
            if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_DIR) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_DIR).isEmpty()) {
                if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_SUBS).isEmpty()) {
                    File vcsRootDir = new File(PropertyUtil.getProjectProperty(PROP_VCS_ROOT_DIR));
                    result = "";
                    for (String subDirName : PropertyUtil.getProjectProperty(PROP_VCS_ROOT_SUBS).split(",")) {
                        File subDir = new File(vcsRootDir, subDirName);
                        long commitNumber = GitUtil.getCheckedoutCommitNumber(subDir);
                        result = "".equals(result) ? result+commitNumber : result+"."+commitNumber;
                    }
                } else {
                    File commitDir = GitUtil.getSubdirWithLatestCommit(new File(PropertyUtil.getProjectProperty(PROP_VCS_ROOT_DIR)));
                    result = GitUtil.getCheckedoutCommitHashShort(commitDir);
                }
            } else {
                result = GitUtil.getCheckedoutCommitHashShort(GitUtil.getGitProjectRoot(projectDir));
            }
        } else if (SvnUtil.isSvnDir(projectDir)) {
            LoggerUtil.info("Svn repo detected.");
            File svnProjectRootFile = SvnUtil.getSvnAbsoluteRoot(projectDir);
            List<File> externalFolders = SvnUtil.getSvnExternalFolders(svnProjectRootFile);
            if (externalFolders.isEmpty()) {
                LoggerUtil.debug("svn external links not found");
                result = SvnUtil.getSvnVersionFromFile(svnProjectRootFile).toString();
            } else {
                LoggerUtil.info("svn external folders found");
                Map<File, Integer> folderRevisions = SvnUtil.getFolderRevisions(externalFolders);
                Map.Entry<File, Integer> revisionEntry = SvnUtil.getLargestRevision(folderRevisions);
                result = revisionEntry.getValue().toString();
                LoggerUtil.info("Largest svn revision '"+revisionEntry.getValue()+"' was found in '"+revisionEntry.getKey()+"'");
            }
        } else {
            String error = "Failed to detect project's version control system, please pass revision/sha1 as a property";
            LoggerUtil.warn(error);
            throw new IOException(error);
        }
        return result;
    }

}
