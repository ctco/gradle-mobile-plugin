/*
 * @(#)ZipUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class ZipUtilTest {

    @Test
    public void testPosixDirectoryCompressionWithoutRoot() throws IOException {
        File testDir;
        do {
            testDir = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        } while (testDir.exists());
        FileUtils.forceMkdir(testDir);
        LoggerUtil.error(testDir.getAbsolutePath());
        if (PosixUtil.isPosixFileStore(testDir)) {
            File root = createAndGetDir(new File(testDir, "archive"));
            File dirL = createAndGetDir(new File(root, "wLink"));
            File dirR = createAndGetDir(new File(root, "wReal"));
            File fileE = new File(dirR, "file.empty");
            FileUtils.touch(fileE);
            File fileR = new File(dirR, "file.real");
            FileUtils.writeStringToFile(fileR, "This is a real file");
            File fileX = new File(dirR, ".DS_Store");
            FileUtils.touch(fileX);
            File fileL = new File(dirL, "file.link");
            Files.createSymbolicLink(fileL.toPath(), fileR.toPath());
            File fileZ = new File(testDir, "woRoot.zip");
            ZipUtil.compressDirectory(root, false, fileZ);
            String detectedContent = getZipArchiveEntryNames(fileZ).toString();
            List<String> expectedList = Arrays.asList("[d]wLink/", "[l]wLink/file.link", "[d]wReal/", "[f]wReal/file.empty", "[f]wReal/file.real");
            Collections.sort(expectedList);
            String expectedContent = expectedList.toString();
            LoggerUtil.error("Detected in archive: "+detectedContent);
            LoggerUtil.error("Expected in archive: "+expectedContent);
            assertTrue(detectedContent.equals(expectedContent));
            //
            root = createAndGetDir(new File(testDir, "extracted"));
            ZipUtil.extractAll(fileZ, root);
            detectedContent = getDirectoryEntryNames(root).toString();
            LoggerUtil.error("Detected in directory: "+detectedContent);
            LoggerUtil.error("Expected in directory: "+expectedContent);
            assertTrue(detectedContent.equals(expectedContent));
        }
    }

    @Test
    public void testPosixDirectoryCompressionWithRoot() throws IOException {
        File testDir;
        do {
            testDir = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        } while (testDir.exists());
        FileUtils.forceMkdir(testDir);
        LoggerUtil.error(testDir.getAbsolutePath());
        if (PosixUtil.isPosixFileStore(testDir)) {
            File root = createAndGetDir(new File(testDir, "archive"));
            File dirL = createAndGetDir(new File(root, "wLink"));
            File dirR = createAndGetDir(new File(root, "wReal"));
            File fileE = new File(dirR, "file.empty");
            FileUtils.touch(fileE);
            File fileR = new File(dirR, "file.real");
            FileUtils.writeStringToFile(fileR, "This is a real file");
            File fileX = new File(dirR, ".DS_Store");
            FileUtils.touch(fileX);
            File fileL = new File(dirL, "file.link");
            Files.createSymbolicLink(fileL.toPath(), fileR.toPath());
            File fileZ = new File(testDir, "wRoot.zip");
            ZipUtil.compressDirectory(root, true, fileZ);
            String detectedContent = getZipArchiveEntryNames(fileZ).toString();
            List<String> expectedList = Arrays.asList("[d]archive/", "[d]archive/wLink/", "[l]archive/wLink/file.link", "[d]archive/wReal/", "[f]archive/wReal/file.empty", "[f]archive/wReal/file.real");
            Collections.sort(expectedList);
            String expectedContent = expectedList.toString();
            LoggerUtil.error("Detected in archive: "+detectedContent);
            LoggerUtil.error("Expected in archive: "+expectedContent);
            assertTrue(detectedContent.equals(expectedContent));
            //
            root = createAndGetDir(new File(testDir, "extracted"));
            ZipUtil.extractAll(fileZ, root);
            detectedContent = getDirectoryEntryNames(root).toString();
            LoggerUtil.error("Detected in directory: "+detectedContent);
            LoggerUtil.error("Expected in directory: "+expectedContent);
            assertTrue(detectedContent.equals(expectedContent));
        }
    }

    private static List<String> getZipArchiveEntryNames(File sourceZip) throws IOException {
        List<String> content = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(sourceZip);) {
            List<ZipArchiveEntry> zipEntries = Collections.list(zipFile.getEntries());
            for (ZipArchiveEntry zipEntry : zipEntries) {
                if (zipEntry.isUnixSymlink()) {
                    content.add("[l]"+zipEntry.getName());
                } else if (zipEntry.isDirectory()) {
                    content.add("[d]"+zipEntry.getName());
                } else {
                    content.add("[f]"+zipEntry.getName());
                }
            }
        }
        Collections.sort(content);
        return content;
    }

    private static List<String> getDirectoryEntryNames(File sourceDir) throws IOException {
        List<String> content = new ArrayList<>();
        for (File file : FileUtils.listFilesAndDirs(sourceDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE)) {
            String relativePath = getRelativePath(sourceDir, file);
            if (Files.isSymbolicLink(file.toPath())) {
                content.add("[l]"+relativePath);
            } else if (file.isDirectory() && !relativePath.isEmpty()) {
                content.add("[d]"+relativePath+"/");
            } else if (file.isFile()) {
                content.add("[f]"+relativePath);
            } else if (!relativePath.isEmpty()) {
                content.add("[?]"+relativePath);
            }
        }
        Collections.sort(content);
        return content;
    }

    private static String getRelativePath(File base, File path) {
        return base.toPath().relativize(path.toPath()).toString();
    }

    private File createAndGetDir(File dir) throws IOException {
        FileUtils.forceMkdir(dir);
        return dir;
    }

}
