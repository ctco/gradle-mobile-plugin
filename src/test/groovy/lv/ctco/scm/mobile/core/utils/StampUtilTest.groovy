/*
 * @(#)StampUtilTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import org.gradle.api.GradleException

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class StampUtilTest {

    //private final String[] validStampArray = {"1","2","3","4"}
    private final String validVersionIteration = "1.2.3"
    private final String validVersionToStamp = "1.2.3.0"
    private final String validVersionIteration1 = "1"
    private final String validStampShort = "1.2.3.4"
    private final String validStampLong = "1.2.3.4.5"
    private final String newStampShort = "1.2.3.5"
    private final String newStampLong = "1.2.3.4.6"
    private final String validStamp1 = "3.4"
    private final String validExtStamp = "4"

    @Test
    public void testGetExternalDeploymentOfStamp() throws Exception {
        String extStamp = StampUtil.getExternalDeploymentOfStamp(validStampShort)
        assertEquals(validExtStamp, extStamp)

        extStamp = StampUtil.getExternalDeploymentOfStamp(validStamp1)
        assertEquals(validExtStamp, extStamp)

        try{
            StampUtil.getExternalDeploymentOfStamp("Zzz")
        } catch (Throwable expected){
            assertTrue(expected instanceof IOException)
            return
        }
        fail("Exception was expected!")
    }

    @Test
    public void testGenerateNewVersionData() throws Exception {
        String stamp = StampUtil.generateNewStampVersion(validStampShort)
        assertEquals(newStampShort, stamp)
        stamp = StampUtil.generateNewStampVersion(validStampLong)
        assertEquals(newStampLong, stamp)
    }

    @Test
    public void testGetIterationVersionFromStamp() throws Exception {
        String iterationVersion = StampUtil.getIterationVersionFromStamp(validStampShort)
        assertEquals(validVersionIteration, iterationVersion)
        iterationVersion = StampUtil.getIterationVersionFromStamp("...1.0.")
        assertEquals(validVersionIteration1, iterationVersion)
    }

    @Test
    public void testIterationToStamp() throws Exception {
        String s = StampUtil.iterationToStamp(validVersionIteration)
        assertEquals(validVersionToStamp, s)
    }

    @Test
    public void sanitiseStampTest() {
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("1.2.3.1"))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp(".1.2.3.1"))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("..1.2.3.1"))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("1.2.3.1."))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("1.2.3.1.."))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp(".1.2.3.1."))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("..1.2.3.1.."))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp(" .1.2.3.1"))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp(" . 1.2.3.1"))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("1.2.3.1. "))
        assertEquals("1.2.3.1", StampUtil.sanitiseStamp("1.2.3.1 . "))
    }

}
