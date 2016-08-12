/*
 * @(#)MobilePluginUtilTest.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MobilePluginUtilTest {

    @Test
    public void pluginHasInfoProperties() throws IOException {
        MobilePluginUtil.getPluginInfo(MobilePluginUtil.class.getClassLoader().getResourceAsStream("plugin-info.properties"));
        assertNotNull(MobilePluginUtil.getPluginGroup());
        assertNotNull(MobilePluginUtil.getPluginName());
        assertNotNull(MobilePluginUtil.getPluginVersion());
        assertEquals("lv.ctco.scm", MobilePluginUtil.getPluginGroup());
        assertEquals("gradle-mobile-plugin", MobilePluginUtil.getPluginName());
    }

}
