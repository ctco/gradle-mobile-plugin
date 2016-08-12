/*
 * @(#)PropertyUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.Project;

public final class PropertyUtil {

    private PropertyUtil() {}

    public static String getProjectProperty(Project project, String property) {
        return project.getProperties().get(property).toString();
    }

    public static boolean hasProjectProperty(Project project, String property) {
        return project.hasProperty(property);
    }

    public static String getEnvironmentProperty(String name) {
        return StringUtils.trimToEmpty(System.getenv(name));
    }

    public static boolean hasEnvironmentProperty(String name) {
        return System.getenv(name) != null;
    }

}
