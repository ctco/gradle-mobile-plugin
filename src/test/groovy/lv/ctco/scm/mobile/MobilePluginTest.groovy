/*
 * @(#)MobilePluginTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile

import lv.ctco.scm.mobile.core.utils.PropertyUtil

import org.junit.Test

class MobilePluginTest {

    @Test
    public void pluginHasInfoProperties() {
        InputStream pluginInfoStream = this.getClass().getClassLoader().getResourceAsStream('plugin-info.properties')
        Properties pluginInfoProperties = new Properties()
        if (pluginInfoStream != null) {
            pluginInfoProperties.load(pluginInfoStream)
            PropertyUtil.setPluginGroup(pluginInfoProperties.getProperty('plugin-group'))
            PropertyUtil.setPluginName(pluginInfoProperties.getProperty('plugin-name'))
            PropertyUtil.setPluginVersion(pluginInfoProperties.getProperty('plugin-version'))
        }
        assert(PropertyUtil.getPluginGroup() != null)
        assert(PropertyUtil.getPluginName() != null)
        assert(PropertyUtil.getPluginVersion() != null)
        assert(PropertyUtil.getPluginGroup().equals("lv.ctco.scm"))
        assert(PropertyUtil.getPluginName().equals("gradle-mobile-plugin"))
    }

}
