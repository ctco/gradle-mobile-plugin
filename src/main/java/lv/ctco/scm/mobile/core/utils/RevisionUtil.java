/*
 * @(#)RevisionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Singleton
public class RevisionUtil {

    private static String revision = null;

    private RevisionUtil() {}

    public static String getRevision() throws IOException {
        if (StringUtils.isBlank(revision)) {
            if (PropertyUtil.hasProjectProperty("revision") && !StringUtils.isBlank(PropertyUtil.getProjectProperty("revision"))) {
                setRevision(GitUtil.getShortHash(PropertyUtil.getProjectProperty("revision")));
                LoggerUtil.info("Revision '"+revision+"' was set from passed property");
            } else {
                setRevision(getGitShortHashOrSvnRevision(PathUtil.getProjectDir()));
                LoggerUtil.info("Revision '"+revision+"' was auto-detected");
            }
        }
        return revision;
    }

    private static void setRevision(String value) {
        revision = value;
    }

    public static void setRevisionIfNull(String value) {
        if (revision == null) {
            revision = value;
        }
    }

    private static String getGitShortHashOrSvnRevision(File projDir) throws IOException {
        String result;
        if (GitUtil.isUnderGit(projDir)) {
            result = GitUtil.getGitShortHash(GitUtil.getGitProjectRoot(projDir));
        } else if (SvnUtil.isSvnDir(projDir)) {
            File svnProjectRootFile = SvnUtil.getSvnAbsoluteRoot(projDir);
            LoggerUtil.info("Svn repo detected.");
            List<File> externalFolders = SvnUtil.getSvnExternalFolders(svnProjectRootFile);
            if (externalFolders.isEmpty()) {
                LoggerUtil.debug("svn external links not found");
                result = SvnUtil.getSvnVersionFromFile(svnProjectRootFile).toString();
            } else {
                LoggerUtil.info("svn external folders found");
                Map<File, Integer> folderRevisions = SvnUtil.getFolderRevisions(externalFolders);
                Map.Entry<File,Integer> revisionEntry = SvnUtil.getLargestRevision(folderRevisions);
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
