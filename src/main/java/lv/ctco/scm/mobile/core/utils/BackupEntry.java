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

public class BackupEntry implements Serializable {

    private File backupedFile;
    private boolean originalExisted;
    private File originalFile;
    private long originalTime;

    public BackupEntry(File file) throws IOException {
        this.originalFile = file.getAbsoluteFile();
        if (file.exists()) {
            this.originalExisted = true;
            this.originalTime = Files.getLastModifiedTime(file.toPath(), LinkOption.NOFOLLOW_LINKS).toMillis();
        } else {
            this.originalExisted = false;
            this.originalTime = 0L;
        }
        do {
            this.backupedFile = new File(PathUtil.getBackupDir(), "/"+UUID.randomUUID());
        } while (this.backupedFile.exists());
    }

    public File getBackupedFile() {
        return backupedFile;
    }

    public void setBackupedFile(File backupedFile) {
        this.backupedFile = backupedFile;
    }

    public boolean hadOriginalExisted() {
        return originalExisted;
    }

    public void setOriginalExisted(boolean originalExisted) {
        this.originalExisted = originalExisted;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    public long getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(long originalTime) {
        this.originalTime = originalTime;
    }

}
