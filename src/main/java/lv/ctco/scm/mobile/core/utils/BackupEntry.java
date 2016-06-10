/*
 * @(#)BackupEntry.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.UUID;

class BackupEntry implements Serializable {

    private File backupedFile;
    private boolean originalExisted;
    private File originalFile;
    private long originalTime;

    BackupEntry(File file) throws IOException {
        this.originalFile = file.getAbsoluteFile();
        if (file.exists()) {
            this.originalTime = Files.getLastModifiedTime(file.toPath(), LinkOption.NOFOLLOW_LINKS).toMillis();
        } else {
            this.originalTime = 0L;
        }
        this.originalExisted = file.exists();
        this.backupedFile = generateNewBackupPath();
    }

    File getBackupedFile() {
        return backupedFile;
    }

    boolean hadOriginalExisted() {
        return originalExisted;
    }

    File getOriginalFile() {
        return originalFile;
    }

    long getOriginalTime() {
        return originalTime;
    }

    private File generateNewBackupPath() throws IOException {
        File newBackupPath;
        do {
            newBackupPath = new File(PathUtil.getBackupDir(), UUID.randomUUID().toString());
        } while (newBackupPath.exists());
        return newBackupPath;
    }

}
