/*
 * @(#)XamarinUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;

@Singleton
public class XamarinUtil {

    private static final String DEFAULT_RELEASE_VERSION = "0.1";

    private XamarinUtil() {}

    public static String getReleaseVersion(Csproj csproj) {
        if (csproj == null || StringUtils.isBlank(csproj.getReleaseVersion())) {
            LoggerUtil.info("Release version not found in Csproj.");
            LoggerUtil.info("Setting  release version as default '"+DEFAULT_RELEASE_VERSION+"'");
            return DEFAULT_RELEASE_VERSION;
        } else {
            return csproj.getReleaseVersion();
        }
    }

}
