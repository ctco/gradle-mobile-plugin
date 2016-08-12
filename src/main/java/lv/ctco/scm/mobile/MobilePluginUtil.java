/*
 * @(#)MobilePluginUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

public class MobilePluginUtil {

    private static String pluginGroup;
    private static String pluginName;
    private static String pluginVersion;

    private MobilePluginUtil() {}

    public static String getPluginGroup() {
        return pluginGroup;
    }

    public static String getPluginName() {
        return pluginName;
    }

    public static String getPluginVersion() {
        return pluginVersion;
    }

    static void getPluginInfo(InputStream pluginInfoStream) throws IOException {
        if (pluginInfoStream != null) {
            Properties pluginInfoProperties = new Properties();
            pluginInfoProperties.load(pluginInfoStream);
            pluginGroup = pluginInfoProperties.getProperty("plugin-group");
            pluginName = pluginInfoProperties.getProperty("plugin-name");
            pluginVersion = pluginInfoProperties.getProperty("plugin-version");
            LoggerUtil.info(pluginGroup + ":" + pluginName + ":" + pluginVersion);
            TeamcityUtil.setAgentParameter("tools.build.plugin.group", pluginGroup);
            TeamcityUtil.setAgentParameter("tools.build.plugin.name", pluginName);
            TeamcityUtil.setAgentParameter("tools.build.plugin.version", pluginVersion);
        }
    }

}
