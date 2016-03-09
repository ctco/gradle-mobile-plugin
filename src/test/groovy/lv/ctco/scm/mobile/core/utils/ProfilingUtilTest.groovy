/*
 * @(#)ProfilingUtilTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertEquals

class ProfilingUtilTest {

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
    public void testprofileUsingSimplePlistBuddyUpdateExisting() throws Exception {
        File infoM = initTemporaryFileFromResource('Info.source.plist')
        File profileM = initTemporaryFileFromResource('Info-minimal.profile.plist')
        File expectedM = initTemporaryFileFromResource('Info-minimal.expected.plist')

        File infoA = initTemporaryFileFromResource('Info.source.plist')
        File profileA = initTemporaryFileFromResource('Info-additional.profile.plist')

        // Default profiling mode UPDATE_EXISTING should update the key value of profile key
        ProfilingUtil.profileUsingPlistEntries(infoM, profileM)
        assertEquals(expectedM.getText().trim(), infoM.getText().trim())

        // Default profiling mode UPDATE_EXISTING should ignore the additional profile key
        ProfilingUtil.profileUsingPlistEntries(infoA, profileA)
        assertEquals(expectedM.getText().trim(), infoA.getText().trim())
    }

    @Test
    public void testprofileUsingSimplePlistBuddyUpdateAndAdd() throws Exception {
        File infoM = initTemporaryFileFromResource('Info.source.plist')
        File profileM = initTemporaryFileFromResource('Info-minimal.profile.plist')
        File expectedM = initTemporaryFileFromResource('Info-minimal.expected.plist')

        File infoA = initTemporaryFileFromResource('Info.source.plist')
        File profileA = initTemporaryFileFromResource('Info-additional.profile.plist')
        File expectedA = initTemporaryFileFromResource('Info-additional.expected.plist')

        // Profiling mode UPDATE_AND_ADD should work as UPDATE_EXISTING if there are no additional keys
        ProfilingUtil.profileUsingPlistEntries(infoM, profileM, ProfilingUtilMode.UPDATE_AND_ADD)
        assertEquals(expectedM.getText().trim(), infoM.getText().trim())

        // Profiling mode UPDATE_AND_ADD should add the additional profile key
        ProfilingUtil.profileUsingPlistEntries(infoA, profileA, ProfilingUtilMode.UPDATE_AND_ADD)
        assertEquals(expectedA.getText().trim(), infoA.getText().trim())
    }

    @Test
    public void testUpdateRootPlistPreferenceSpecifiersKeyDefaultValue() throws Exception {
        File rootM = initTemporaryFileFromResource('Root.source.plist')
        File expectedM = initTemporaryFileFromResource('Root.expected.plist')

        ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootM, 'AutocapitalizationType', 'Minimal')
        assertEquals(expectedM.getText().trim(), rootM.getText().trim())
    }

}
