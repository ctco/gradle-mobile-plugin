/*
 * @(#)XandroidExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.PlatformExtension

class XandroidExtension extends PlatformExtension {

    /**
     * Automatic configuration currently is unsupported for Android extension
     */
    boolean automaticConfiguration = false

    /**
     * Android target specific extension properties
     */
    File projectFile

    String javaXmx
    String javaOpts

    String signingKeystore
    String signingCertificateAlias

    String androidVersionCode

    public boolean isValid() {
        boolean result = true
        if (solutionFile == null) {
            result = false;
        }
        if (projectFile == null) {
            result = false
        }
        if (projectName == null) {
            result = false
        }
        if (environments.size() == 0) {
            result = false
        }
        return result;
    }

}
