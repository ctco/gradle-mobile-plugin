/*
 * @(#)PathUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Deprecated // (since = "0.15.1.2", forRemoval = true)
public final class PathUtil {

    private static File userDir = FileUtils.getUserDirectory();
    private static File projectDir = new File(".").getAbsoluteFile();
    private static File buildDir = new File(".", "build").getAbsoluteFile();

    private static final String BACKUP_SUBPATH = "backups";
    private static final String TEMP_SUBPATH = "temp";

    private static final String BUILDLOG_SUBPATH = "logs";

    private static final String REPORT_SUMMARY_SUBPATH = "reports/summary";
    private static final String REPORT_UNIT_SUBPATH = "reports/unit";

    private static final String IPADIST_SUBPATH = "ipadist";
    private static final String DSYMDIST_SUBPATH = "dsymdist";
    private static final String APKDIST_SUBPATH = "apkdist";
    private static final String AARDIST_SUBPATH = "aardist";
    private static final String ARTIFACT_SUBPATH = "artifacts";

    private static final String XCODE_DST_SUBPATH = "xcodebuild/dst";
    private static final String XCODE_OBJ_SUBPATH = "xcodebuild/obj";
    private static final String XCODE_SYM_SUBPATH = "xcodebuild/sym";
    private static final String XCODE_SHARED_SUBPATH = "xcodebuild/shared";

    private static final String DEFAULT_PROVISIONING_PROFILE_SUBPATH = "Library/MobileDevice/Provisioning Profiles";

    private PathUtil() {}

    public static File getUserDir() {
        return userDir;
    }

    public static File getProjectDir() {
        return projectDir;
    }

    public static File getBuildDir() throws IOException {
        return createAndReturn(buildDir);
    }

    public static File getBackupDir() throws IOException {
        try {
            return createAndReturn(new File(getBuildDir(), BACKUP_SUBPATH));
        } catch (IOException e) {
            throw new IOException("Failed to get backup directory!", e);
        }
    }

    public static File getTempDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), TEMP_SUBPATH));
    }

    public static File getBuildlogDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), BUILDLOG_SUBPATH));
    }

    public static File getReportSummaryDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), REPORT_SUMMARY_SUBPATH));
    }

    public static File getReportUnitDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), REPORT_UNIT_SUBPATH));
    }

    public static File getIpaDistDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), IPADIST_SUBPATH));
    }

    public static File getDsymDistDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), DSYMDIST_SUBPATH));
    }

    public static File getApkDistDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), APKDIST_SUBPATH));
    }

    public static File getAarDistDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), AARDIST_SUBPATH));
    }

    public static File getPublicationArtifactDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), ARTIFACT_SUBPATH));
    }

    public static File getXcodeDstDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), XCODE_DST_SUBPATH));
    }

    public static File getXcodeObjDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), XCODE_OBJ_SUBPATH));
    }

    public static File getXcodeSymDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), XCODE_SYM_SUBPATH));
    }

    public static File getXcodeSharedDir() throws IOException {
        return createAndReturn(new File(getBuildDir(), XCODE_SHARED_SUBPATH));
    }

    public static File getAndroidKeystore() {
        return new File(getUserDir(), ".gradle/init.d/android.keystore");
    }

    public static File getDefaultProvisioningProfileDir() {
        return new File(getUserDir(), DEFAULT_PROVISIONING_PROFILE_SUBPATH);
    }

    private static File createAndReturn(File dir) throws IOException {
        FileUtils.forceMkdir(dir);
        return dir;
    }

}
