/*
 * @(#)ProfilingUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProfilingUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() throws Exception {
        BackupUtil.restoreAllFiles();
    }

    private String getTrimmedFileContent(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8).trim();
    }

    private File initTemporaryFileFromResource(String resourceName) throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(resourceName);
        if (resource != null) {
            Path resourceFile = Paths.get(resource.toURI());
            Path temporaryFile = new File(tempFolder.getRoot(), resourceName).toPath();
            Files.createDirectories(temporaryFile.getParent());
            Files.copy(resourceFile, temporaryFile);
            return temporaryFile.toFile();
        } else {
            throw new IOException("Resource file '"+resourceName+"' was not found");
        }
    }

    @Test
    public void testProfileUsingPlistEntriesUpdateExistingUpdate() throws Exception {
        File actual = initTemporaryFileFromResource("Info.source.plist");
        File profile = initTemporaryFileFromResource("Info-minimal.profile.plist");
        File expected = initTemporaryFileFromResource("Info-minimal.expected.plist");
        // Default profiling mode UPDATE_EXISTING should update the key value of profile key
        ProfilingUtil.profileFirstLevelPlistEntries(actual, profile);
        assertEquals(getTrimmedFileContent(expected), getTrimmedFileContent(actual));
    }

    @Test
    public void testProfileUsingPlistEntriesUpdateExistingIgnore() throws Exception {
        File actual = initTemporaryFileFromResource("Info.source.plist");
        File profile = initTemporaryFileFromResource("Info-additional.profile.plist");
        File expected = initTemporaryFileFromResource("Info-minimal.expected.plist");
        // Default profiling mode UPDATE_EXISTING should ignore the additional profile key
        ProfilingUtil.profileFirstLevelPlistEntries(actual, profile);
        assertEquals(getTrimmedFileContent(expected), getTrimmedFileContent(actual));
    }

    @Test
    public void testProfileUsingPlistEntriesUpdateAndAddUpdate() throws Exception {
        File infoM = initTemporaryFileFromResource("Info.source.plist");
        File profileM = initTemporaryFileFromResource("Info-minimal.profile.plist");
        File expectedM = initTemporaryFileFromResource("Info-minimal.expected.plist");
        // Profiling mode UPDATE_AND_ADD should work as UPDATE_EXISTING if there are no additional keys
        ProfilingUtil.profileFirstLevelPlistEntries(infoM, profileM, ProfilingUtilMode.UPDATE_AND_ADD);
        assertEquals(getTrimmedFileContent(expectedM), getTrimmedFileContent(infoM));
    }

    @Test
    public void testProfileUsingPlistEntriesUpdateAndAddAdd() throws Exception {
        File infoA = initTemporaryFileFromResource("Info.source.plist");
        File profileA = initTemporaryFileFromResource("Info-additional.profile.plist");
        File expectedA = initTemporaryFileFromResource("Info-additional.expected.plist");
        // Profiling mode UPDATE_AND_ADD should add the additional profile key
        ProfilingUtil.profileFirstLevelPlistEntries(infoA, profileA, ProfilingUtilMode.UPDATE_AND_ADD);
        assertEquals(getTrimmedFileContent(expectedA), getTrimmedFileContent(infoA));
    }

    @Test
    public void testUpdateRootPlistPreferenceSpecifiersKeyDefaultValue() throws Exception {
        File actual = initTemporaryFileFromResource("Root.source.plist");
        File expected = initTemporaryFileFromResource("Root.expected.plist");
        ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(actual, "AutocapitalizationType", "Minimal");
        assertEquals(getTrimmedFileContent(expected), getTrimmedFileContent(actual));
    }

    @Test
    public void testGetPreferenceSpecifiersObjectArray() throws IOException, URISyntaxException {
        File actual = initTemporaryFileFromResource("Settings.bundle/Root.plist");
        List<NSObject> array = ProfilingUtil.getPreferenceSpecifiersObjectArray(actual);
        assertNotNull(array);
        assertEquals(11, array.size());
        for (NSObject nsObject : array) {
            NSDictionary nsDictionary = (NSDictionary) nsObject;
            System.out.println("dictKey = "+nsDictionary.get("Type")+" '"+nsDictionary.get("Key")+"'");
        }
    }

    @Test
    public void testProfiledPreferenceSpecifiersObjectArray () throws IOException, URISyntaxException {
        File actualPlist = initTemporaryFileFromResource("Settings.bundle/Root.plist");
        File profilePlist = initTemporaryFileFromResource("Profiles/Root-PROD.plist");
        File expectedPlist = initTemporaryFileFromResource("Expected/Root-PROD.plist");

        List<NSObject> actualArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(actualPlist);
        List<NSObject> profileArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(profilePlist);
        List<NSObject> expectedArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(expectedPlist);

        List<NSObject> resultArray = ProfilingUtil.getProfiledPreferenceSpecifiersObjectArray(actualArray, profileArray);
        assertEquals(expectedArray, resultArray);
        ProfilingUtil.profilePreferenceSpecifiersPlistEntries(actualPlist, profilePlist);
        assertEquals(getTrimmedFileContent(expectedPlist), getTrimmedFileContent(actualPlist));
    }

    @Ignore
    @Test
    public void testConvertPlistFromXmlToBinary() throws IOException, URISyntaxException {
        File targetPlist = initTemporaryFileFromResource("Expected/Root-PROD.plist");
        File expectPlist = initTemporaryFileFromResource("Expected/Root-PROD.binary.plist");

        PlistUtil.resaveAsBinaryPlist(targetPlist);
        List<NSObject> targetArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(targetPlist);
        List<NSObject> expectArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(expectPlist);

        assertEquals(expectArray, targetArray);
        assertEquals(getTrimmedFileContent(expectPlist), getTrimmedFileContent(targetPlist));
        assertEquals(CommonUtil.getMD5Hex(expectPlist), CommonUtil.getMD5Hex(targetPlist));
    }

    @Ignore
    @Test
    public void testConvertPlistFromBinaryToXml() throws IOException, URISyntaxException {
        File targetPlist = initTemporaryFileFromResource("Expected/Root-PROD.binary.plist");
        File expectPlist = initTemporaryFileFromResource("Expected/Root-PROD.plist");

        PlistUtil.resaveAsXmlPlist(targetPlist);
        List<NSObject> targetArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(targetPlist);
        List<NSObject> expectArray = ProfilingUtil.getPreferenceSpecifiersObjectArray(expectPlist);

        assertEquals(expectArray, targetArray);
        assertEquals(getTrimmedFileContent(expectPlist), getTrimmedFileContent(targetPlist));
    }

}
