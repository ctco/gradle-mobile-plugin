/*
 * @(#)VersionUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionUtilTest {

    @Test
    public void testVersionIncrementation() {
        assertEquals("2", VersionUtil.generateIncrementedVersion("1"));
        assertEquals("1.3", VersionUtil.generateIncrementedVersion("1.2"));
        assertEquals("1.2.4", VersionUtil.generateIncrementedVersion("1.2.3"));
        assertEquals("1.2.3.5", VersionUtil.generateIncrementedVersion("1.2.3.4"));
        assertEquals("1.2.3.4.6", VersionUtil.generateIncrementedVersion("1.2.3.4.5"));
    }

}
