/*
 * @(#)ZipUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp;
import org.apache.commons.compress.archivers.zip.X7875_NewUnix;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ZipUtil {

    private static final Logger logger = Logging.getLogger(ZipUtil.class);

    private static final List<String> DEFAULT_EXCLUDES = Collections.singletonList(".DS_Store");

    private ZipUtil() {}

    public static void extractAll(File sourceZip, File outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(sourceZip)) {
            List<ZipArchiveEntry> zipArchiveEntries = Collections.list(zipFile.getEntries());
            for (ZipArchiveEntry zipArchiveEntry : zipArchiveEntries) {
                extract(zipArchiveEntry, zipFile, outputDir);
            }
        }
    }

    public static void extract(String zipArchiveEntryName, File sourceZip, File outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(sourceZip)) {
            ZipArchiveEntry zipArchiveEntry = zipFile.getEntry(zipArchiveEntryName);
            extract(zipArchiveEntry, zipFile, outputDir);
        }
    }

    private static void extract(ZipArchiveEntry zipArchiveEntry, ZipFile zipFile, File outputDir) throws IOException {
        File extractedFile = new File(outputDir, zipArchiveEntry.getName());
        FileUtils.forceMkdir(extractedFile.getParentFile());
        if (zipArchiveEntry.isUnixSymlink()) {
            if (PosixUtil.isPosixFileStore(outputDir)) {
                logger.debug("Extracting [l] "+zipArchiveEntry.getName());
                String symlinkTarget = zipFile.getUnixSymlink(zipArchiveEntry);
                Files.createSymbolicLink(extractedFile.toPath(), new File(symlinkTarget).toPath());
            } else {
                logger.debug("Skipping ! [l] "+zipArchiveEntry.getName());
            }
        } else if (zipArchiveEntry.isDirectory()) {
            logger.debug("Extracting [d] "+zipArchiveEntry.getName());
            FileUtils.forceMkdir(extractedFile);
        } else {
            logger.debug("Extracting [f] "+zipArchiveEntry.getName());
            try (
                    InputStream in = zipFile.getInputStream(zipArchiveEntry);
                    OutputStream out = new FileOutputStream(extractedFile)
            ) {
                IOUtils.copy(in, out);
            }
        }
        updatePermissions(extractedFile, zipArchiveEntry.getUnixMode());
    }

    private static void updatePermissions(File file, int unixMode) throws IOException {
        if (!Files.isSymbolicLink(file.toPath()) && PosixUtil.isPosixFileStore(file)) {
            Set<PosixFilePermission> permissions = PosixUtil.getPosixPermissionsAsSet(unixMode);
            if (!permissions.isEmpty()) {
                Files.setPosixFilePermissions(file.toPath(), permissions);
            }
        }
    }

    public static void compressDirectory(File rootDir, boolean includeAsRoot, File output) throws IOException {
        if (!rootDir.isDirectory()) {
            throw new IOException("Provided file is not a directory");
        }
        try (OutputStream archiveStream = new FileOutputStream(output);
             ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream)) {
            String rootName = "";
            if (includeAsRoot) {
                insertDirectory(rootDir, rootDir.getName(), archive);
                rootName = rootDir.getName()+"/";
            }
            Collection<File> fileCollection = FileUtils.listFilesAndDirs(rootDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
            for (File file : fileCollection) {
                String relativePath = getRelativePath(rootDir, file);
                String entryName = rootName+relativePath;
                if (!relativePath.isEmpty() && !"/".equals(relativePath)) {
                    insertObject(file, entryName, archive);
                }
            }
            archive.finish();
        } catch (IOException | ArchiveException e) {
            throw new IOException(e);
        }
    }

    private static void insertObject(File file, String entryName, ArchiveOutputStream archive) throws IOException {
        if (Files.isSymbolicLink(file.toPath())) {
            insertSymlink(file, entryName, Files.readSymbolicLink(file.toPath()).toString(), archive);
        } else if (file.isDirectory()) {
            insertDirectory(file, entryName, archive);
        } else if (file.isFile()) {
            insertFile(file, entryName, archive);
        }
    }

    private static void insertSymlink(File file, String entryName, String linkTarget, ArchiveOutputStream archive) throws IOException {
        if (Files.isSymbolicLink(file.toPath())) {
            ZipArchiveEntry newEntry = new ZipArchiveEntry(entryName);
            setExtraFields(file.toPath(), UnixStat.LINK_FLAG, newEntry);
            archive.putArchiveEntry(newEntry);
            archive.write(linkTarget.getBytes());
            archive.closeArchiveEntry();
        } else {
            throw new IOException("Provided file is not a symlink");
        }
    }

    private static void insertDirectory(File dir, String entryName, ArchiveOutputStream archive) throws IOException {
        if (dir.isDirectory()) {
            ZipArchiveEntry newEntry = new ZipArchiveEntry(entryName+"/");
            setExtraFields(dir.toPath(), UnixStat.DIR_FLAG, newEntry);
            archive.putArchiveEntry(newEntry);
            archive.closeArchiveEntry();
        } else {
            throw new IOException("Provided file is not a directory");
        }
    }

    private static void insertFile(File file, String entryName, ArchiveOutputStream archive) throws IOException {
        if (DEFAULT_EXCLUDES.contains(file.getName())) {
            logger.debug("Skipping ! [l] {}", entryName);
            return;
        }
        if (file.isFile()) {
            ZipArchiveEntry newEntry = new ZipArchiveEntry(entryName);
            setExtraFields(file.toPath(), UnixStat.FILE_FLAG, newEntry);
            archive.putArchiveEntry(newEntry);
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        } else {
            throw new IOException("Provided file is not a file");
        }
    }

    private static void setExtraFields(Path filePath, int fileType, ZipArchiveEntry entry) throws IOException {
        if (PosixUtil.isPosixFileStore(filePath)) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(filePath);
            entry.setUnixMode(fileType | PosixUtil.getPosixPermissionsAsInt(perms));
            X5455_ExtendedTimestamp x5455 = getX5455(filePath);
            X7875_NewUnix x7875 = getX7875(filePath);
            if (x5455 != null && x7875 != null) {
                entry.setExtraFields(new ZipExtraField[] {x5455, x7875});
            }
        }
    }

    private static X5455_ExtendedTimestamp getX5455(Path filePath) throws IOException {
        X5455_ExtendedTimestamp x5455 = null;
        if (PosixUtil.isPosixFileStore(filePath)) {
            Map<String, Object> fileAttr = getFileAttributes(filePath);
            if (fileAttr.get("lastModifiedTime") != null && fileAttr.get("lastAccessTime") != null) {
                x5455 = new X5455_ExtendedTimestamp();
                x5455.setModifyJavaTime(new Date(((FileTime)fileAttr.get("lastModifiedTime")).toMillis()));
                x5455.setAccessJavaTime(new Date(((FileTime)fileAttr.get("lastAccessTime")).toMillis()));
            }
        }
        return x5455;
    }

    private static X7875_NewUnix getX7875(Path filePath) throws IOException {
        X7875_NewUnix x7875 = null;
        if (PosixUtil.isPosixFileStore(filePath)) {
            Map<String, Object> fileAttr = getFileAttributes(filePath);
            if (fileAttr.get("gid") != null && fileAttr.get("uid") != null) {
                x7875 = new X7875_NewUnix();
                x7875.setGID(Long.valueOf(fileAttr.get("gid").toString()));
                x7875.setUID(Long.valueOf(fileAttr.get("uid").toString()));
            }
        }
        return x7875;
    }

    private static Map<String, Object> getFileAttributes(Path path) throws IOException {
        return Files.readAttributes(path, "unix:*", LinkOption.NOFOLLOW_LINKS);
    }

    private static String getRelativePath(File base, File path) {
        return base.toPath().relativize(path.toPath()).toString().replace("\\", "/");
    }

}
