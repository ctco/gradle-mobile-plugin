/*
 * @(#)MobilePluginUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.util.GradleVersion;

import java.io.IOException;

public class MobilePluginUtil {

    private static final Logger logger = Logging.getLogger(MobilePluginUtil.class);

    private static String pluginName = MobilePluginUtil.class.getPackage().getImplementationTitle();
    private static String pluginVersion = MobilePluginUtil.class.getPackage().getImplementationVersion();

    private static final String ANDROID_APP_PLUGIN_ID = "com.android.application";
    private static final String ANDROID_LIB_PLUGIN_ID = "com.android.library";

    private MobilePluginUtil() {}

    public static String getPluginName() {
        return pluginName;
    }

    public static String getPluginVersion() {
        return pluginVersion;
    }

    static void announcePluginApply(String pluginName, String projectName) {
        String pluginVersion = MobilePluginUtil.getPluginVersion();
        logger.info("Applying '{}:{}' to '{}' project", pluginName, pluginVersion, projectName);
    }

    static void announcePluginIgnore(String pluginName, String projectName) {
        String pluginVersion = MobilePluginUtil.getPluginVersion();
        logger.warn("Ignoring '{}:{}' on '{}' project", pluginName, pluginVersion, projectName);
    }

    static void checkMinimumGradleVersion() throws IOException {
        GradleVersion gradleVersionMinimum = GradleVersion.version("2.14.1");
        GradleVersion gradleVersionCurrent = GradleVersion.current();
        if (gradleVersionCurrent.compareTo(gradleVersionMinimum) > 0) {
            throw new IOException("Execution on older Gradle version ("+gradleVersionCurrent.getVersion()+
                    ") than is defined as the minimum ("+gradleVersionMinimum.getVersion()+")");
        }
    }

    public static boolean isAndroidAppPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(ANDROID_APP_PLUGIN_ID);
    }

    public static boolean isAndroidLibraryPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(ANDROID_LIB_PLUGIN_ID);
    }

}
