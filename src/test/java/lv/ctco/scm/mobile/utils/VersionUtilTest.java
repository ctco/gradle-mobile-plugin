/*
 * @(#)VersionUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class VersionUtilTest {

    @Test
    public void testIsValidVersionString() {
        assertFalse(VersionUtil.isValidVersionString(null));
        assertFalse(VersionUtil.isValidVersionString(""));
        assertFalse(VersionUtil.isValidVersionString("."));
        assertFalse(VersionUtil.isValidVersionString("1.0.0-beta"));
        assertFalse(VersionUtil.isValidVersionString("1.0.0-SNAPSHOT"));
        assertTrue(VersionUtil.isValidVersionString("1"));
        assertTrue(VersionUtil.isValidVersionString("1.2"));
        assertTrue(VersionUtil.isValidVersionString("1.2.3"));
        assertTrue(VersionUtil.isValidVersionString("1.2.3.4"));
        assertTrue(VersionUtil.isValidVersionString("1.2.3.4.5"));
    }

    @Test
    public void testIsMajorMinorPatchVersion() {
        assertTrue(VersionUtil.isMajorMinorPatchVersion("1.2.3"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1.2"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1.2.3.4"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1-SNAPSHOT"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1.2-SNAPSHOT"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1.2.3-SNAPSHOT"));
        assertFalse(VersionUtil.isMajorMinorPatchVersion("1.2.3.4-SNAPSHOT"));
    }

    @Test
    public void testNormalizeToMajorMinorPatchVersion() {
        try {
            assertEquals("1.0.0", VersionUtil.normalizeToMajorMinorPatchVersion("1"));
            assertEquals("1.2.0", VersionUtil.normalizeToMajorMinorPatchVersion("1.2"));
            assertEquals("1.2.3", VersionUtil.normalizeToMajorMinorPatchVersion("1.2.3"));
            assertEquals("1.2.3", VersionUtil.normalizeToMajorMinorPatchVersion("1.2.3.4"));
            assertEquals("1.2.3", VersionUtil.normalizeToMajorMinorPatchVersion("1.2.3.4.5"));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testGenerateIncrementedVersion() {
        try{
            assertEquals("2", VersionUtil.generateIncrementedVersion("1"));
            assertEquals("1.3", VersionUtil.generateIncrementedVersion("1.2"));
            assertEquals("1.2.4", VersionUtil.generateIncrementedVersion("1.2.3"));
            assertEquals("1.2.3.5", VersionUtil.generateIncrementedVersion("1.2.3.4"));
            assertEquals("1.2.3.4.6", VersionUtil.generateIncrementedVersion("1.2.3.4.5"));
        } catch (IOException e) {
            fail();
        }
    }

}
