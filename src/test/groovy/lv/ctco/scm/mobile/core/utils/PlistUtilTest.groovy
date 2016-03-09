/*
 * @(#)PlistUtilTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PlistUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @After
    public void tearDown() throws Exception {
        BackupUtil.restoreAllFiles()
    }

    protected File initTemporaryFileFromResource(String resourceName) {
        File temporaryFile = tempFolder.newFile()
        File resourceFile = new File(this.getClass().getClassLoader().getResource(resourceName).toURI())
        temporaryFile << resourceFile.getText()
        return temporaryFile
    }

    @Test
    public void readStringValueFromPlist() {
        File infoPlist = initTemporaryFileFromResource('Info.read.plist')
        String version = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString")
        assert(version.equals("1.0"))
    }

    @Test
    public void addNewStringValueInPlist() {
        File infoPlist = initTemporaryFileFromResource('Info.read.plist')
        String newKey = "new"
        String expectedValue = "2.0"
        PlistUtil.setStringValue(infoPlist, newKey, expectedValue)
        String newValue = PlistUtil.getStringValue(infoPlist, newKey)
        assert(newValue.equals(expectedValue))
    }

    @Test
    public void updateExistingStringValueInPlist() {
        File infoPlist = initTemporaryFileFromResource('Info.read.plist')
        String existingKey = "CFBundleShortVersionString"
        String expectedValue = "2.0"
        PlistUtil.setStringValue(infoPlist, existingKey, expectedValue)
        String readValue = PlistUtil.getStringValue(infoPlist, existingKey)
        assert(readValue.equals(expectedValue))
    }

    @Test
    public void readDateValueFromPlist() {
        File infoPlist = initTemporaryFileFromResource('Info.read.plist')
        Date expectedDate = new Date(116,5,4,11,6,27)
        Date foundDate = PlistUtil.getDateValue(infoPlist, "ExpirationDate")
        assert(foundDate.equals(expectedDate))
    }

}
