/*
 * @(#)NugetUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class NugetUtil {

    private NugetUtil() {}

    static boolean hasCacheDirConfig(File projectFile) throws IOException {
        boolean result = false;
        File nugetConfig = new File(projectFile.getParentFile(), "nuget.config");
        if (nugetConfig.exists()) {
            for (String line : FileUtils.readLines(nugetConfig, StandardCharsets.UTF_8)) {
                if (line.trim().startsWith("<add key=\"repositoryPath\"")) {
                    result = true;
                }
            }
        }
        return result;
    }

    static File getCacheDirPath(File projectFile) throws IOException {
        File nugetConfig = new File(projectFile.getParentFile(), "nuget.config");
        File resultingCacheDir = PathUtil.getDefaultNugetCacheDir();
        if (nugetConfig.exists()) {
            for (String line : FileUtils.readLines(nugetConfig, StandardCharsets.UTF_8)) {
                if (line.trim().startsWith("<add key=\"repositoryPath\"")) {
                    resultingCacheDir = new File(line.substring(line.indexOf("value=")+7, line.indexOf("\" />")));
                }
            }
        }
        return resultingCacheDir;
    }

}
