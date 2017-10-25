/*
 * @(#)BackupUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.io.FileUtils;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;

public final class BackupUtil {

    private static final Logger logger = Logging.getLogger(BackupUtil.class);

    private static File backupIndex;
    private static ArrayList<BackupEntry> backupEntries;

    private static final BackupUtil instance = new BackupUtil();

    private BackupUtil() {
        logger.debug("Initializing BackupUtil");
        try {
            backupIndex = new File(PathUtil.getBackupDir(), "backup.index");
            if (backupIndex.exists()) {
                loadBackupData();
            } else {
                backupEntries = new ArrayList<>();
                saveBackupData();
            }
            showBackupFiles();
        } catch (IOException e) {
            throw new GradleException("Exception initializing BackupUtil", e);
        }
    }

    public static BackupUtil getInstance() {
        return instance;
    }

    public static boolean isBackuped(File targetFile) {
        return getBackupEntry(targetFile) != null;
    }

    private static void saveBackupData() throws IOException {
        try (
                FileOutputStream fileOut = new FileOutputStream(backupIndex);
                ObjectOutputStream streamOut = new ObjectOutputStream(fileOut)
        ) {
            streamOut.writeObject(backupEntries);
        }
    }

    private static void loadBackupData() throws IOException {
        try (
                FileInputStream fileIn = new FileInputStream(backupIndex);
                ObjectInputStream streamIn = new ObjectInputStream(fileIn)
        ) {
            backupEntries = (ArrayList<BackupEntry>)streamIn.readObject();
        }  catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private static void showBackupFiles() {
        logger.debug("Detected {} backuped files", backupEntries.size());
        for (BackupEntry backupEntry : backupEntries) {
            logger.debug(backupEntry.getOriginalFile().getAbsolutePath());
        }
    }

    public static void backupFile(File file) throws IOException {
        if (!isBackuped(file)) {
            BackupEntry newEntry = new BackupEntry(file);
            if  (newEntry.hadOriginalExisted()) {
                logger.info("  Backing up '{}'", newEntry.getOriginalFile());
                FileUtils.copyFile(newEntry.getOriginalFile(), newEntry.getBackupedFile());
            }
            backupEntries.add(newEntry);
            saveBackupData();
        }
    }

    public static void restoreAllFiles() throws IOException {
        if (!backupEntries.isEmpty()) {
            logger.info("Restoring all profiling modifications...");
            Iterator<BackupEntry> entryIterator = backupEntries.iterator();
            while (entryIterator.hasNext()) {
                BackupEntry backupEntry = entryIterator.next();
                if (backupEntry.hadOriginalExisted()) {
                    restoreFile(backupEntry.getOriginalFile(), false);
                } else {
                    FileUtils.forceDelete(backupEntry.getOriginalFile());
                }
                entryIterator.remove();
                saveBackupData();
            }
        }
    }

    public static void applyChanges() throws IOException {
        if (!backupEntries.isEmpty()) {
            logger.info("Applying all profiling modifications...");
            Iterator<BackupEntry> entryIterator = backupEntries.iterator();
            while (entryIterator.hasNext()) {
                BackupEntry backupEntry = entryIterator.next();
                if (backupEntry.hadOriginalExisted()) {
                    logger.debug("  Removing backup '{}'", backupEntry.getBackupedFile().getAbsolutePath());
                    FileUtils.forceDelete(backupEntry.getBackupedFile());
                }
                entryIterator.remove();
                saveBackupData();
            }
        }
    }

    private static void restoreFile(File path, boolean removeFromList) throws IOException {
        BackupEntry backupEntry = getBackupEntry(path);
        if (backupEntry == null) {
            throw new IOException("Restorable file not in index");
        }
        logger.info("  Restoring '{}'", path);
        logger.info("    current md5={}", CommonUtil.getMD5Hex(path));
        if (backupEntry.hadOriginalExisted()) {
            FileUtils.copyFile(backupEntry.getBackupedFile(), backupEntry.getOriginalFile());
            Files.setLastModifiedTime(backupEntry.getOriginalFile().toPath(), FileTime.fromMillis(backupEntry.getOriginalTime()));
            FileUtils.forceDelete(backupEntry.getBackupedFile());
        }
        logger.info("    initial md5={}", CommonUtil.getMD5Hex(path));
        if (removeFromList) {
            backupEntries.remove(backupEntry);
            saveBackupData();
        }
    }

    private static BackupEntry getBackupEntry(File path) {
        for (BackupEntry backupEntry : backupEntries) {
            if (backupEntry.getOriginalFile().getAbsoluteFile().equals(path.getAbsoluteFile())) {
                return backupEntry;
            }
        }
        return null;
    }

}
