/*
 * @(#)XamarinExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.PlatformExtension
import lv.ctco.scm.mobile.core.utils.LoggerUtil

class XamarinExtension extends PlatformExtension {

    /**
     * Validate plist syntax with plutil lint command
     * after profiling and versionUpdate tasks for all build configurations
     * if set as true.
     * false by default to keep logic compatibility.
     */
    boolean enforcePlistSyntax = false;

    /**
     * Skip adding updateVersion task for build configurations
     * that are configured with Appstore|iPhone
     * if set as true.
     * false by default to keep logic compatibility.
     */
    boolean skipUpdateVersionForAppstoreConfiguration = false;

    /**
     * Update Info plist key CFBundleShortVersionString with clean version
     * if set as true.
     * false by default to keep logic compatibility.
     */
    boolean updateCFBundleShortVersionString = false;

    public boolean isValid() {
        boolean result = true;
        if (assemblyName == null) {
            LoggerUtil.error("assemblyName for ctcoMobile.xamarin extension is not defined");
            result = false;
        }
        if (projectBaseName == null) {
            LoggerUtil.error("projectBaseName for ctcoMobile.xamarin extension is not defined");
            result = false;
        }
        if (projectName == null) {
            LoggerUtil.error("projectName for ctcoMobile.xamarin extension is not defined");
            result = false;
        }
        return result;
    }

}
