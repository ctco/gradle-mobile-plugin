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
public final class CommonUtil {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private CommonUtil() {}

    /**
     * Prints Teamcity service messages.
     * @param releaseVersion Project releaseVersion.
     * @throws IOException .
     */
    public static void printTeamcityInfo(String releaseVersion) throws IOException {
        String revision = RevisionUtil.getRevision();
        if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_SUBS).isEmpty()) {
            TeamcityUtil.setBuildNumber(releaseVersion+"."+revision);
            TeamcityUtil.setAgentParameter("build.number", releaseVersion+"."+revision);
        } else {
            TeamcityUtil.setBuildNumber(releaseVersion+"_"+revision);
            TeamcityUtil.setAgentParameter("build.number", releaseVersion+"_"+revision);
        }
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

    static File getIpaPayloadApp(File dir) {
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

    static File unpackIpaPayload(File targetIpa, File payloadDir) throws IOException {
        LoggerUtil.info("Unpacking ["+targetIpa.getAbsolutePath()+"] payload to ["+payloadDir.getAbsolutePath()+"]");
        FileUtils.deleteDirectory(payloadDir);
        ZipUtil.extractAll(targetIpa, payloadDir);
        return payloadDir;
    }

    static void repackIpaPayload(File payloadDir, File targetIpa) throws IOException {
        LoggerUtil.info("Repacking ["+payloadDir.getAbsolutePath()+"] payload to ["+targetIpa.getAbsolutePath()+"]");
        if (payloadDir.exists()) {
            ZipUtil.compressDirectory(payloadDir, false, targetIpa);
        } else {
            throw new IOException("Payload ["+payloadDir.getAbsolutePath()+"] was not found!");
        }
    }

}
