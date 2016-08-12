/*
 * @(#)ReprofilingUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.gradle.api.Project;

import lv.ctco.scm.mobile.core.objects.IosApp;
import lv.ctco.scm.mobile.core.objects.Profile;

import java.io.File;
import java.io.IOException;

public final class ReprofilingUtil {

    private ReprofilingUtil() {}

    private static boolean hasOnlySupportedProfiles(String targetEnv, Profile[] profiles) {
        boolean result = true;
        for (Profile profile : profiles) {
            if (targetEnv.equalsIgnoreCase(profile.getEnvironment()) && profile.hasScope("artifact")) {
                String[] sources = profile.getSources().split(",");
                for (String source : sources) {
                    if (!source.toLowerCase().endsWith(".plist")) {
                        LoggerUtil.warn("Can only execute standard plist source reprofiling!");
                        result = false;
                    }
                }
                if (profile.getTarget() == null) {
                    LoggerUtil.warn("Can only execute standard plist source reprofiling!");
                    result = false;
                }
                if (!("info.plist".equalsIgnoreCase(profile.getTarget())
                        || "Settings.bundle/Root.plist".equalsIgnoreCase(profile.getTarget())
                        || "archived-expanded-entitlements.xcent".equalsIgnoreCase(profile.getTarget()))) {
                    LoggerUtil.warn("Can only execute standard defined plist source reprofiling!");
                    result = false;
                }
            }
        }
        return result;
    }

    private static String getCleanVersion(String providedVersion) {
        if (providedVersion.contains("_")) {
            return providedVersion.substring(0, providedVersion.indexOf('_'));
        } else {
            return providedVersion;
        }
    }

    public static void reprofileIpa(Project project, File targetIpa, String targetEnv, Profile[] profiles, boolean cleanReleaseVersionForPROD) throws IOException {
        LoggerUtil.info("Reprofiling IPA '"+targetIpa.getAbsolutePath()+"' to environment '"+targetEnv+"'");
        File workingDir = targetIpa.getParentFile();
        File payloadDir = new File(workingDir, "temp_ipa_payload");
        if (targetIpa.exists() && hasOnlySupportedProfiles(targetEnv, profiles)) {
            CommonUtil.unpackIpaPayload(targetIpa, payloadDir);
            File appDir = CommonUtil.getIpaPayloadApp(payloadDir);
            for (Profile profile : profiles) {
                if (targetEnv.equalsIgnoreCase(profile.getEnvironment()) && profile.hasScope("artifact")) {
                    File targetFile = new File(appDir, profile.getTarget());
                    File profileFile = new File("Profiles/"+profile.getSources());
                    if ("archived-expanded-entitlements.xcent".equalsIgnoreCase(targetFile.getName())) {
                        ProfilingUtil.profileUsingPlistEntries(targetFile, profileFile, ProfilingUtilMode.UPDATE_AND_ADD);
                        CommonUtil.addNewlineAtEndOfFile(targetFile);
                    } else {
                        ProfilingUtil.profileUsingPlistEntries(targetFile, profileFile);
                    }
                }
            }
            if (PropertyUtil.hasProjectProperty(project, "reprofiling.version")) {
                String version = PropertyUtil.getProjectProperty(project, "reprofiling.version");
                if (version.isEmpty()) {
                    LoggerUtil.warn("Reprofiling version has not been provided. Will use the existing version.");
                } else {
                    LoggerUtil.info("Updating artifact version to "+version);
                    File infoPlist = new File(appDir, "Info.plist");
                    File rootPlist = new File(appDir, "Settings.bundle/Root.plist");
                    if (cleanReleaseVersionForPROD && "PROD".equalsIgnoreCase(targetEnv)) {
                        version = getCleanVersion(version);
                    }
                    PlistUtil.setStringValue(infoPlist, "CFBundleVersion", version);
                    ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlist, "application_version", version);
                    PlistUtil.convertPlistToBinaryFormat(infoPlist);
                    PlistUtil.validatePlist(infoPlist);
                    PlistUtil.validatePlist(rootPlist);
                }
            } else {
                LoggerUtil.warn("Reprofiling version has not been provided. Will use the existing version.");
            }
            BackupUtil.applyChanges();
            IosSigningUtil.signApp(project, appDir);
            IosApp iosApp = new IosApp(appDir);
            BuildReportUtil.addIosAppInfo(iosApp);
            File resultIpa;
            if (targetIpa.getName().contains(" ")) {
                resultIpa = new File(targetIpa.getName().substring(0, targetIpa.getName().indexOf(' '))+' '+targetEnv+".ipa");
            } else {
                resultIpa = new File(targetIpa.getName().substring(0, targetIpa.getName().indexOf(".ipa"))+targetEnv+".ipa");
            }
            CommonUtil.repackIpaPayload(payloadDir, resultIpa);
        } else {
            throw new IOException("Resign target '"+targetIpa.getAbsolutePath()+"' was not found!");
        }
    }

}
