/*
 * @(#)MobileExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

class MobileExtension {

    /** What mobile platform to use. Allowed values are: xcode, xamarin. */
    String platform;

    /** The directory where all the resulting IPA artifacts should be copied. Deprecated in favor of PathUtil. */
    @Deprecated
    File ipaDistDir;

    /** The directory where all the resulting APK artifacts should be copied. Deprecated in favor of PathUtil. */
    @Deprecated
    File apkDistDir;

    /** The directory where all the resulting dSYM artifacts should be copied. Deprecated in favor of PathUtil. */
    @Deprecated
    File dsymDistDir;

}
