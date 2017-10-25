/*
 * @(#)PlistUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class PlistUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() throws Exception {
        BackupUtil.restoreAllFiles();
    }

    private File initTemporaryFileFromResource(String resourceName) throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(resourceName);
        if (resource != null) {
            Path resourceFile = Paths.get(resource.toURI());
            Path temporaryFile = new File(tempFolder.getRoot(), resourceName).toPath();
            Files.copy(resourceFile, temporaryFile);
            return temporaryFile.toFile();
        } else {
            throw new IOException("Resource file '"+resourceName+"' was not found");
        }
    }

    @Test
    public void readStringValueFromPlist() throws IOException, URISyntaxException {
        File infoPlist = initTemporaryFileFromResource("Info.read.plist");
        String actual = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
        assertEquals("1.0", actual);
    }

    @Test
    public void addNewStringValueInPlist() throws IOException, URISyntaxException {
        File infoPlist = initTemporaryFileFromResource("Info.read.plist");
        String key = "CustomShortVersionString";
        String expected = "2.0";
        PlistUtil.setStringValue(infoPlist, key, expected);
        String actual = PlistUtil.getStringValue(infoPlist, key);
        assertEquals(expected, actual);
    }

    @Test
    public void updateExistingStringValueInPlist() throws IOException, URISyntaxException {
        File infoPlist = initTemporaryFileFromResource("Info.read.plist");
        String key = "CFBundleShortVersionString";
        String expected = "2.0";
        PlistUtil.setStringValue(infoPlist, key, expected);
        String actual = PlistUtil.getStringValue(infoPlist, key);
        assertEquals(expected, actual);
    }

    @Test
    public void readDateValueFromPlist() throws IOException, URISyntaxException {
        File infoPlist = initTemporaryFileFromResource("Info.read.plist");
        Date expected = new Date(1465027587000L);
        Date actual = PlistUtil.getDateValue(infoPlist, "ExpirationDate");
        assertEquals(expected, actual);
    }

}
