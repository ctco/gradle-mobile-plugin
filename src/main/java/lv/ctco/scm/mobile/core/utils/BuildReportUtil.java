/*
 * @(#)BuildReportUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import lv.ctco.scm.mobile.MobilePluginUtil;
import lv.ctco.scm.mobile.core.objects.IosApp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public final class BuildReportUtil {

    private static List<IosApp> iosAppList = new ArrayList<>();

    private BuildReportUtil() {}

    public static void addIosAppInfo(IosApp iosApp) throws IOException {
        iosAppList.add(iosApp);
        saveReport();
    }

    private static void saveReport() throws IOException {
        File reportFile = new File(PathUtil.getReportSummaryDir(), "build-info.html");
        Files.deleteIfExists(reportFile.toPath());
        FileUtils.touch(reportFile);
        writeStyle(reportFile);
        FileUtils.write(reportFile, "<html><body><table>", StandardCharsets.UTF_8, true);
        writeSysEnvInfo(reportFile);
        for (IosApp iosApp : iosAppList) {
            writeIosAppInfo(iosApp, reportFile);
        }
        FileUtils.write(reportFile, "</table><body></html>", StandardCharsets.UTF_8, true);
    }

    private static void writeSysEnvInfo(File reportFile) throws IOException {
        writeReportHeader(reportFile, "Build environment");
        writeReportEntry(reportFile, "Build plugin version", MobilePluginUtil.getPluginVersion());
        /*
        if (PropertyUtil.hasEnvironmentProperty("TEAMCITY_VERSION")) {
            writeReportEntry(reportFile, "Xcode version", PropertyUtil.getEnvironmentProperty("XCODE_VERSION"));
            writeReportEntry(reportFile, "iOS SDK version", PropertyUtil.getEnvironmentProperty("XCODE_SDK_IPHONEOS"));
            writeReportEntry(reportFile, "Mono framework version", PropertyUtil.getEnvironmentProperty("XAMARIN_MONO_VERSION"));
            writeReportEntry(reportFile, "Xamarin Studio version", PropertyUtil.getEnvironmentProperty("XAMARIN_VERSION"));
            writeReportEntry(reportFile, "Xamarin.iOS version", PropertyUtil.getEnvironmentProperty("XAMARIN_IOS_VERSION"));
            writeReportEntry(reportFile, "Xamarin.Android version", PropertyUtil.getEnvironmentProperty("XAMARIN_ANDROID_VERSION"));
        }
        */
    }

    private static void writeIosAppInfo(IosApp iosApp, File reportFile) throws IOException {
        writeReportHeader(reportFile, iosApp.getName()+" (iOS App)");
        writeReportEntry(reportFile, "Bundle name", iosApp.getBundleName());
        writeReportEntry(reportFile, "Bundle identifier", iosApp.getBundleIdentifier());
        writeReportEntry(reportFile, "Bundle build version", iosApp.getBundleVersion());
        writeReportEntry(reportFile, "Bundle release version", iosApp.getBundleVersionShort());
        writeReportEntry(reportFile, "Build configuration", iosApp.getBuildCnf());
        writeReportEntry(reportFile, "Build SDK", iosApp.getBuildSdk());
        writeReportEntry(reportFile, "Provisioning name", iosApp.getProvisioningProfileName());
        writeReportEntry(reportFile, "Provisioning UUID", iosApp.getProvisioningUuid());
        writeReportEntry(reportFile, "Provisioning expiration", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(iosApp.getProvisioningExpiration()));
        writeReportEntry(reportFile, "Provisioning team name", iosApp.getProvisioningTeamName());
        writeReportEntry(reportFile, "Identity name", iosApp.getIdentityName());
        writeReportEntry(reportFile, "Identity type", iosApp.getIdentityType());
    }

    private static void writeStyle(File reportFile) throws IOException {
        FileUtils.write(reportFile, "<style type=\"text/css\">", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table {border:none; display: table; border-collapse: separate; border-spacing: 2px; border-color: grey; font-family: \"Helvetica Neue\", Arial, sans-serif; font-size: 82%; margin-top: 10px; margin-left: 10px; margin-right: 10px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table tbody th {border:none; text-align: center; font-weight: bolder; border-left: none; border-right: none; border-top: none; border-bottom: 1px dotted #ccc; padding: 5px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table td {border-left: none; border-right: none; border-top: none; border-bottom: 1px dotted #ccc; padding: 5px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "</style>", StandardCharsets.UTF_8, true);
    }

    private static void writeReportHeader(File reportFile, String header) throws IOException {
        FileUtils.write(reportFile, "<tr><th colspan=\"2\">"+header+"</th></tr>", StandardCharsets.UTF_8, true);
    }

    private static void writeReportEntry(File reportFile, String varName, String varValue) throws IOException {
        FileUtils.write(reportFile, "<tr><td>"+varName+"</td><td>"+varValue+"</td></tr>", StandardCharsets.UTF_8, true);
    }

}
