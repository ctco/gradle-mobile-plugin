/*
 * @(#)XamarinUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class XamarinUtil {

    private static final String DEFAULT_RELEASE_VERSION = "0.1";

    private XamarinUtil() {}

    public static String getReleaseVersion(Csproj csproj) {
        String csprojName = csproj.getFile().getName();
        String releaseVersion = csproj.getReleaseVersion();
        if (StringUtils.isBlank(releaseVersion)) {
            LoggerUtil.info("Release version not found in "+csprojName);
            releaseVersion = DEFAULT_RELEASE_VERSION;
        } else {
            LoggerUtil.info("Release version was found in "+csprojName);
        }
        LoggerUtil.info("Setting release version as '"+releaseVersion+"' for builds of '"+csprojName+"'");
        return releaseVersion;
    }

    public static int getSlnCount(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, null);
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isFile() && file.getParentFile().equals(dir)
                        && "sln".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results.size();
    }

}
