/*
 * @(#)ProfilingUtilEvalTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse

class ProfilingUtilEvalTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    GroovyProfilingEval profiling

    @Before
    public void setUp() throws Exception {
        profiling = new GroovyProfilingEval()
    }

    @After
    public void tearDown() throws Exception {
        BackupUtil.restoreAllFiles()
    }

    @Test
    public void testCopyBackupReplaceWorkflow() throws Exception {
        File profileFile = tempFolder.newFile("profile.txt")
        profileFile << "Hello World"
        File targetFile = tempFolder.newFile("target.txt")

        //Testing file copy and backup
        profiling.copyFile(profileFile.getCanonicalPath(), targetFile.getCanonicalPath())
        assertEquals targetFile.text, profileFile.text
        // TODO : DISABLED Check to remove
        //assert new File(tempFolder.root.canonicalPath + "/target.txt.original").exists()
        //assertTrue(BackupUtil.isBackuped(targetFile))

        //Testing simple replacement
        profiling.replaceInFile(targetFile.getCanonicalPath(), "World", "Tester")
        assertEquals targetFile.text, "Hello Tester"

        //Testing regexp replacement
        profiling.replaceInFile(targetFile.getCanonicalPath(), "([H])\\w+", "Goodbye")
        assertEquals targetFile.text, "Goodbye Tester"
    }

    @Test
    public void testCopyToNonexistingTarget() throws Exception {
        File profileFile = tempFolder.newFile("profile.txt")
        profileFile << "Hello World"
        File targetFile = tempFolder.newFile("target.txt")
        //Removing target file to check copy to nonexisting file path
        targetFile.delete()

        //Testing file copy and backup-ignore
        profiling.copyFile(profileFile.getCanonicalPath(), targetFile.getCanonicalPath())
        assertEquals targetFile.text, profileFile.text
        assert new File(tempFolder.root.canonicalPath + "/target.txt").exists()
        // TODO : DISABLED Check to remove
        //assertFalse new File(tempFolder.root.canonicalPath + "/target.txt.original").exists()
        //assertTrue(BackupUtil.isBackuped(targetFile))

        //Testing simple replacement
        profiling.replaceInFile(targetFile.getCanonicalPath(), "World", "Tester")
        assertEquals targetFile.text, "Hello Tester"

        //Testing regexp replacement
        profiling.replaceInFile(targetFile.getCanonicalPath(), "([H])\\w+", "Goodbye")
        assertEquals targetFile.text, "Goodbye Tester"

        BackupUtil.restoreAllFiles()
        assertFalse new File(tempFolder.root.canonicalPath + "/target.txt").exists()
    }
}
