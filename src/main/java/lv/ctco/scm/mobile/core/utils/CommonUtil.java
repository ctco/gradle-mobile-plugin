/*
 * @(#)CommonUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public final class CommonUtil {

    private static final Logger logger = Logging.getLogger(CommonUtil.class);

    private CommonUtil() {}

    public static void replaceInFile(File file, Pattern patternToFind, String replaceWith) throws IOException {
        BackupUtil.backupFile(file);
        logger.info("Replacing requested pattern in file '{}'", file.getAbsolutePath());
        String content = FileUtils.readFileToString(file, Charsets.UTF_8);
        content = content.replaceAll(patternToFind.pattern(), replaceWith);
        FileUtils.writeStringToFile(file, content, Charsets.UTF_8, false);
        logger.info("  current md5={}", getMD5Hex(file));
    }

    public static void addNewlineAtEndOfFile(File file) throws IOException {
        BackupUtil.backupFile(file);
        FileUtils.write(file, System.lineSeparator(), Charsets.UTF_8, true);
    }

    public static List<File> findIosDsymsinDirectory(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isDirectory() && "dsym".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    public static List<File> findIosAppsInDirectory(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isDirectory() && "app".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    public static List<File> findIosIpasInDirectory(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        if (!files.isEmpty()) {
            for (File file : files) {
                if ("ipa".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    public static List<File> findAndroidAppsInDirectory(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        if (!files.isEmpty()) {
            for (File file : files) {
                if ("apk".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    static String getMD5Hex(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5hex = DigestUtils.md5Hex(fis);
        fis.close();
        return md5hex;
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
        FileUtils.deleteDirectory(payloadDir);
        ZipUtil.extractAll(targetIpa, payloadDir);
        return payloadDir;
    }

    static void repackIpaPayload(File payloadDir, File targetIpa) throws IOException {
        if (payloadDir.exists()) {
            ZipUtil.compressDirectory(payloadDir, false, targetIpa);
        } else {
            throw new IOException("Payload '"+payloadDir.getAbsolutePath()+"' was not found!");
        }
    }

}
