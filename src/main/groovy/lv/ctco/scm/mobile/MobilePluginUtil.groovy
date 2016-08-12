/*
 * @(#)MobilePluginUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import javax.inject.Singleton;

@Singleton
public final class MobilePluginUtil {

    private static String pluginGroup;
    private static String pluginName;
    private static String pluginVersion;

    private MobilePluginUtil() {
        getPluginInfo(MobilePluginUtil.class.getClassLoader().getResourceAsStream("plugin-info.properties"));
    }

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
