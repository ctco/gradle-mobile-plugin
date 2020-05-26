/*
 * @(#)MobilePluginUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.concurrent.TimeUnit;

public class MobilePluginUtil {

    private static final Logger logger = Logging.getLogger(MobilePluginUtil.class);

    private static final String PACKAGE_TITLE = MobilePluginUtil.class.getPackage().getImplementationTitle();
    private static final String PACKAGE_VERSION = MobilePluginUtil.class.getPackage().getImplementationVersion();

    private static final String ANDROID_APP_PLUGIN_ID = "com.android.application";
    private static final String ANDROID_LIB_PLUGIN_ID = "com.android.library";

    private MobilePluginUtil() {}

    public static String getPackageTitle() {
        return PACKAGE_TITLE;
    }

    public static String getPackageVersion() {
        return PACKAGE_VERSION;
    }

    static void announcePluginApply(String pluginName, String projectName) {
        String pluginVersion = MobilePluginUtil.getPackageVersion();
        logger.info("Applying '{}:{}' to '{}' project", pluginName, pluginVersion, projectName);
    }

    static void announcePluginIgnore(String pluginName, String projectName) {
        String pluginVersion = MobilePluginUtil.getPackageVersion();
        logger.warn("Ignoring '{}:{}' on '{}' project", pluginName, pluginVersion, projectName);
    }

    public static boolean isAndroidAppPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(ANDROID_APP_PLUGIN_ID);
    }

    public static boolean isAndroidLibraryPluginApplied(Project project) {
        return project.getPluginManager().hasPlugin(ANDROID_LIB_PLUGIN_ID);
    }

    public static void createXdepsDependencyConfiguration(Project project) {
        Configuration xdeps;
        try {
            xdeps = project.getConfigurations().getByName("xdeps");
        } catch (UnknownConfigurationException e) {
            xdeps = project.getConfigurations().create("xdeps");
        }
        // https://docs.gradle.org/current/userguide/dependency_management.html#sub:dynamic_versions_and_changing_modules
        xdeps.getResolutionStrategy().cacheChangingModulesFor(0, TimeUnit.SECONDS);
    }

}
