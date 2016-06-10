/*
 * @(#)PropertyUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;

import javax.inject.Singleton;

@Singleton
public final class PropertyUtil {

    private static Project project;

    private static String pluginGroup;
    private static String pluginName;
    private static String pluginVersion;

    private PropertyUtil() {}

    public static void setProject(Project object) {
        project = object;
    }

    public static String getPluginGroup() {
        return pluginGroup;
    }

    public static void setPluginGroup(String value) {
        pluginGroup = value;
    }

    public static String getPluginName() {
        return pluginName;
    }

    public static void setPluginName(String value) {
        pluginName = value;
    }

    public static String getPluginVersion() {
        return pluginVersion;
    }

    public static void setPluginVersion(String value) {
        pluginVersion = value;
    }

    public static String getProjectProperty(String name) {
        return project.getProperties().get(name).toString();
    }

    public static boolean hasProjectProperty(String name) {
        return project.hasProperty(name);
    }

    public static String getEnvironmentProperty(String name) {
        return StringUtils.trimToEmpty(System.getenv(name));
    }

    public static boolean hasEnvironmentProperty(String name) {
        return System.getenv(name) != null;
    }

}
