/*
 * @(#)PosixUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class PosixUtilTest {

    private static final int O400 = 256;
    private static final int O200 = 128;
    private static final int O100 = 64;
    private static final int O040 = 32;
    private static final int O020 = 16;
    private static final int O010 = 8;
    private static final int O004 = 4;
    private static final int O002 = 2;
    private static final int O001 = 1;

    private static final int O777 = 511;
    private static final int O755 = 493;
    private static final int O644 = 420;

    @Test
    public void testPosixPermissionConversions() {
        assertEquals(O400, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O400)));
        assertEquals(O200, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O200)));
        assertEquals(O100, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O100)));
        assertEquals(O040, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O040)));
        assertEquals(O020, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O020)));
        assertEquals(O010, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O010)));
        assertEquals(O004, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O004)));
        assertEquals(O002, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O002)));
        assertEquals(O001, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O001)));

        assertEquals(O777, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O777)));
        assertEquals(O755, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O755)));
        assertEquals(O644, PosixUtil.getPosixPermissionsAsInt(PosixUtil.getPosixPermissionsAsSet(O644)));
    }

}
