/*
 * @(#)BuildReportUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class BuildReportUtil {

    private final Project project;

    public BuildReportUtil(Project project) {
        this.project = project;
    }

    private void writeStyle(File reportFile) throws IOException {
        FileUtils.write(reportFile, "<style type=\"text/css\">", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table {border:none; display: table; border-collapse: separate; border-spacing: 2px; border-color: grey; font-family: \"Helvetica Neue\", Arial, sans-serif; font-size: 82%; margin-top: 10px; margin-left: 10px; margin-right: 10px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table tbody th {border:none; text-align: center; font-weight: bolder; border-left: none; border-right: none; border-top: none; border-bottom: 1px dotted #ccc; padding: 5px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "table td {border-left: none; border-right: none; border-top: none; border-bottom: 1px dotted #ccc; padding: 5px;}", StandardCharsets.UTF_8, true);
        FileUtils.write(reportFile, "</style>", StandardCharsets.UTF_8, true);
    }

    private void writeReportHeader(File reportFile, String header) throws IOException {
        FileUtils.write(reportFile, "<tr><th colspan=\"2\">"+header+"</th></tr>", StandardCharsets.UTF_8, true);
    }

    private void writeReportEntry(File reportFile, String varName, String varValue) throws IOException {
        FileUtils.write(reportFile, "<tr><td>"+varName+"</td><td>"+varValue+"</td></tr>", StandardCharsets.UTF_8, true);
    }

    private void writeIosAppInfo(IosApp iosApp, File reportFile) throws IOException {
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

    private File getReportSummaryDir() throws IOException {
        File reportsSummaryDir = new File(project.getBuildDir(), "reports/summary");
        FileUtils.forceMkdir(reportsSummaryDir);
        return reportsSummaryDir;
    }

    public void writeReportForIosApp(IosApp iosApp, String reportName) throws IOException {
        File reportFile = new File(getReportSummaryDir(), reportName+"-build-info.html");
        Files.deleteIfExists(reportFile.toPath());
        FileUtils.touch(reportFile);
        writeStyle(reportFile);
        FileUtils.write(reportFile, "<html><body><table>", StandardCharsets.UTF_8, true);
        writeIosAppInfo(iosApp, reportFile);
        FileUtils.write(reportFile, "</table><body></html>", StandardCharsets.UTF_8, true);
    }

}
