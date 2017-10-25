/*
 * @(#)VersionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.lang3.StringUtils;

public final class VersionUtil {

    private VersionUtil() {}

    public static String generateIncrementedVersion(String version) {
        String [] parts = StringUtils.split(version, '.');
        Integer last = Integer.parseInt(parts[parts.length - 1]) + 1;
        parts[parts.length - 1] = last.toString();
        return StringUtils.join(parts, '.');
    }

}
