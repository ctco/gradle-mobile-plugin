/*
 * @(#)VersionUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VersionUtil {

    private VersionUtil() {}

    public static boolean isSnapshotVersion(String version) {
        return StringUtils.endsWithIgnoreCase(version, "-SNAPSHOT");
    }

    public static boolean isValidVersionString(String version) {
        try {
            getVersionParts(version);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean isMajorMinorPatchVersion(String version) {
        try {
            return getVersionParts(version).size() == 3;
        } catch (IOException e) {
            return false;
        }
    }

    public static String normalizeToMajorMinorPatchVersion(String version) throws IOException {
        List<Integer> parts = getVersionParts(version);
        switch (parts.size()) {
            case 1:
                parts.add(0);
                parts.add(0);
                break;
            case 2:
                parts.add(0);
                break;
            case 3:
                break;
            default:
                parts = parts.subList(0, 3);
        }
        return StringUtils.join(parts, '.');
    }

    public static String generateIncrementedVersion(String version) throws IOException {
        List<Integer> parts = getVersionParts(version);
        int lastIndex = parts.size()-1;
        Integer lastValue = parts.get(lastIndex);
        parts.set(lastIndex, lastValue+1);
        return StringUtils.join(parts, '.');
    }

    private static List<Integer> getVersionParts(String version) throws IOException {
        String ioErrorMessage = "Invalid version string";
        if (version == null) {
            throw new IOException(ioErrorMessage);
        }
        List<Integer> parts = new ArrayList<>();
        String[] strings = StringUtils.split(version, '.');
        for (String string : strings) {
            try {
                parts.add(Integer.parseInt(string));
            } catch (NumberFormatException e) {
                throw new IOException(ioErrorMessage);
            }
        }
        if (parts.isEmpty() || parts.contains(null)) {
            throw new IOException(ioErrorMessage);
        }
        return parts;
    }

}
