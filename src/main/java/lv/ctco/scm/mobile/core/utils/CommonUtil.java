/*
 * @(#)CommonUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.codec.digest.DigestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Singleton
public class CommonUtil {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private CommonUtil() {}

    /**
     * Prints Teamcity service messages.
     * @param releaseVersion Project releaseVersion.
     * @throws IOException
     */
    public static void printTeamcityInfo(String releaseVersion) throws IOException {
        String revision = RevisionUtil.getRevision();
        TeamcityUtil.setBuildNumber(releaseVersion+"_"+revision);
        TeamcityUtil.setAgentParameter("build.number", releaseVersion+"_"+revision);
        TeamcityUtil.setAgentParameter("project.version.iteration", releaseVersion);
        if (PropertyUtil.hasProjectProperty("stamp")) {
            StampUtil.updateStamp(PropertyUtil.getProjectProperty("stamp"), releaseVersion);
        }
    }

    public static void replaceInFile(File file, Pattern patternToFind, String replaceWith) throws IOException {
        BackupUtil.backupFile(file);
        LoggerUtil.info("Replacing requested pattern in file '"+file.getAbsolutePath()+"'");
        String content = FileUtils.readFileToString(file, DEFAULT_ENCODING);
        content = content.replaceAll(patternToFind.pattern(), replaceWith);
        FileUtils.writeStringToFile(file, content, DEFAULT_ENCODING, false);
        LoggerUtil.info("Updated file "+getMD5InfoString(file));
    }

    public static void addNewlineAtEndOfFile(File file) throws IOException {
        BackupUtil.backupFile(file);
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), DEFAULT_ENCODING));
        writer.write(System.lineSeparator());
        writer.flush();
        writer.close();
        LoggerUtil.info("Updated file "+getMD5InfoString(file));
    }

    public static List<File> findIosDsymsinDirectory(File dir) {
        List<File> dsyms = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && "dsym".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    dsyms.add(file);
                }
            }
        }
        return dsyms;
    }

    public static List<File> findIosAppsInDirectory(File dir) {
        List<File> apps = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && "app".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    apps.add(file);
                }
            }
        }
        return apps;
    }

    public static List<File> findIosIpasInDirectory(File dir) {
        List<File> apps = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if ("ipa".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    apps.add(file);
                }
            }
        }
        return apps;
    }

    public static List<File> findAndroidAppsInDirectory(File dir) {
        List<File> apps = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if ("apk".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    apps.add(file);
                }
            }
        }
        return apps;
    }

    private static String getMD5Hex(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5hex = DigestUtils.md5Hex(fis);
        fis.close();
        return md5hex;
    }

    public static String getMD5InfoString(File file) throws IOException {
        return "["+file.getAbsolutePath()+"](MD5="+getMD5Hex(file)+")";
    }

    public static File getIpaPayloadApp(File dir) {
        File payloadDir = new File(dir, "Payload");
        File[] files = payloadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".app")) {
                    return file;
                }
            }
        }
        return null;
    }

    public static File unpackIpaPayload(File targetIpa, File payloadDir) throws IOException {
        LoggerUtil.info("Unpacking ["+targetIpa.getAbsolutePath()+"] payload to ["+payloadDir.getAbsolutePath()+"]");
        FileUtils.deleteDirectory(payloadDir);
        ZipUtil.extractAll(targetIpa, payloadDir);
        return payloadDir;
    }

    public static void repackIpaPayload(File payloadDir, File targetIpa) throws IOException {
        LoggerUtil.info("Repacking ["+payloadDir.getAbsolutePath()+"] payload to ["+targetIpa.getAbsolutePath()+"]");
        if (payloadDir.exists()) {
            ZipUtil.compressDirectory(payloadDir, false, targetIpa);
        } else {
            throw new IOException("Payload ["+payloadDir.getAbsolutePath()+"] was not found!");
        }
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param ver1 a string of ordinal numbers separated by decimal points.
     * @param ver2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    public static Integer versionCompare(String ver1, String ver2) {
        String[] vals1 = ver1.split("\\.");
        String[] vals2 = ver2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        } else {
            // the strings are equal or one string is a substring of the other
            // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
            return Integer.signum(vals1.length - vals2.length);
        }
    }

}
