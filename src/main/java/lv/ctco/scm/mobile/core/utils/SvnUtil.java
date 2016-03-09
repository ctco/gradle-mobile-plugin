/*
 * @(#)SvnUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.CommandLine;

import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public class SvnUtil {

    private static final String SVN_PROTOCOL_URL = "svn:";
    private static final String SVN_RELATIVE_URL = "^/";

    private SvnUtil() {}

    protected static boolean isSvnDir(File dir) {
        boolean result = false;
        ExecResult execResult = getSvnInfoOutput(dir);
        if (execResult.isSuccess() && execResult.getOutput().get(0).startsWith("Path:")) {
            result = true;
        }
        return result;
    }

    protected static File getSvnAbsoluteRoot(File dir) {
        File result = dir;
        File tmpRootFile = dir;
        String tmpRoot;
        while (isSvnDir(tmpRootFile)) {
            tmpRoot = getSvnProjectRoot(tmpRootFile);
            result = new File(tmpRoot);
            tmpRootFile = new File(tmpRoot, "/..");
        }
        return result;
    }

    protected static Map<File, Integer> getFolderRevisions(List<File> extLinks) {
        Map<File, Integer> linksRevision = new HashMap<>();
        for (File link : extLinks) {
            linksRevision.put(link, getSvnVersionFromFile(link));
        }
        return linksRevision;
    }

    protected static Map.Entry<File, Integer> getLargestRevision(Map<File, Integer> revisions) {
        Map.Entry<File, Integer> revision = null;
        for (Map.Entry<File, Integer> entry : revisions.entrySet()) {
            Integer tmpRevision = entry.getValue();
            if (revision == null || revision.getValue() < tmpRevision) {
                revision = entry;
            }
        }
        return revision;
    }

    protected static Integer getSvnVersionFromFile(File dir) {
        String result = "0";
        if (isSvnDir(dir)) {
            ExecResult execResult = getSvnInfoOutput(dir);
            result = parseSvnInfoForRevision(execResult.getOutput());
        }
        return new Integer(result);
    }

    protected static List<File> getSvnExternalFolders(File dir) {
        List<File> extLinks = new ArrayList<>();
        if (isSvnDir(dir)) {
            ExecResult execResult = getSvnExternalsOutput(dir);
            //List<String> extList = Arrays.asList(commandOutput.trim().split(System.lineSeparator()));
            if (execResult.isSuccess()) {
                String extFolder = "";
                for (String line : execResult.getOutput()) {
                    if (line.contains(" - ")) {
                        int extFolderPositions = line.indexOf(" - ");
                        extFolder = line.substring(0, extFolderPositions).trim();
                        if ("\\.".equals(extFolder)) {
                            extFolder = "";
                        }
                    }
                    if (line.contains(SVN_PROTOCOL_URL) || line.contains(SVN_RELATIVE_URL)) {
                        String link = cleanupExtLinkLine(line);
                        if (link.contains(" ")) {
                            String[] split = link.split(" ");
                            File svnExt;
                            if (extFolder.isEmpty()) {
                                svnExt = new File(dir, split[1]);
                            } else {
                                String tmpFile = new File(dir, extFolder).getAbsolutePath();
                                svnExt = new File(tmpFile,split[1]);
                            }
                            extLinks.add(svnExt);
                            LoggerUtil.debug("Adding svn ext: "+svnExt);
                        }
                    }
                }
            }
        }
        return extLinks;
    }

    private static String parseSvnInfoForRevision(List<String> svnInfo) {
        String result = "";
        for (String line : svnInfo) {
            if (line.startsWith("Last Changed Rev")) {
                if (line.contains(":")) {
                    line = line.substring(line.indexOf(':')+1, line.length()).trim();
                }
                result = line.replace("P", "").replace("S", "").replace("M", "");
                break;
            }
        }
        return result;
    }

    private static String getSvnProjectRoot(File dir) {
        ExecResult execResult = getSvnInfoOutput(dir);
        if (execResult.isSuccess()) {
            return parseSvnInfoForRoot(execResult.getOutput());
        } else {
            return "";
        }
    }

    private static String parseSvnInfoForRoot(List<String> svnInfo) {
        String result = "";
        for (String line : svnInfo) {
            if (line.startsWith("Working Copy Root Path")) {
                if (line.contains(":")) {
                    line = line.substring(line.indexOf(':')+1, line.length()).trim();
                }
                result = line;
                break;
            }
        }
        return result;
    }

    private static String cleanupExtLinkLine(String line) {
        String link = line;
        if (line.contains(SVN_PROTOCOL_URL)) {
            link = getSubString(line, SVN_PROTOCOL_URL);
        }
        if (line.contains(SVN_RELATIVE_URL)) {
            link = getSubString(line, SVN_RELATIVE_URL);
        }
        return link;
    }

    private static String getSubString(String line, String key) {
        int positions = line.indexOf(key);
        return line.substring(positions).trim();
    }

    /**
     * Returns SVN information about SVN externals for a directory.
     * @param dir Directory for which to get SVN externals.
     * @return ExecResult object.
     */
    private static ExecResult getSvnExternalsOutput(File dir) {
        CommandLine commandLine = new CommandLine("svn");
        commandLine.addArguments(new String[] {"propget", "svn:externals", "-R"}, false);
        return ExecUtil.execCommand(commandLine, dir, null, true, false);
    }

    /**
     * Returns SVN info command output for a directory.
     * @param dir Directory for which to get SVN info.
     * @return ExecResult object.
     */
    private static ExecResult getSvnInfoOutput(File dir) {
        CommandLine commandLine = new CommandLine("svn");
        commandLine.addArgument("info", false);
        return ExecUtil.execCommand(commandLine, dir, null, true, false);
    }

}
