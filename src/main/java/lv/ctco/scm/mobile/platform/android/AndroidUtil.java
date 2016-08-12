/*
 * @(#)AndroidUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import org.gradle.api.Project;

public final class AndroidUtil {

    private static final String APP_PLUGIN_CLASS = "com.android.build.gradle.AppPlugin";
    private static final String LIB_PLUGIN_CLASS = "com.android.build.gradle.LibraryPlugin";

    private AndroidUtil() {}

    public static boolean isAndroidPluginApplied(Project project) {
        for (Object plugin : project.getPlugins().toArray()) {
            if (APP_PLUGIN_CLASS.equals(plugin.getClass().getCanonicalName())
                    || LIB_PLUGIN_CLASS.equals(plugin.getClass().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    static boolean isAndroidAppPluginApplied(Project project) {
        for (Object plugin : project.getPlugins().toArray()) {
            if (APP_PLUGIN_CLASS.equals(plugin.getClass().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    static boolean isAndroidLibraryPluginApplied(Project project) {
        for (Object plugin : project.getPlugins().toArray()) {
            if (LIB_PLUGIN_CLASS.equals(plugin.getClass().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

}
