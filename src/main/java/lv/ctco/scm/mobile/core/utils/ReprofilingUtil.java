/*
 * @(#)ReprofilingUtil.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Riga LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Project;

import lv.ctco.scm.mobile.core.objects.IosApp;
import lv.ctco.scm.mobile.core.objects.Profile;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ReprofilingUtil {

    private static final Logger logger = Logging.getLogger(ReprofilingUtil.class);

    private static final String PLIST_INFO = "Info.plist";
    private static final String PLIST_ROOT = "Settings.bundle/Root.plist";
    private static final String PLIST_ENTS = "archived-expanded-entitlements.xcent";

    private static final List<String> updatableTargets = new ArrayList<>(Arrays.asList(
            PLIST_INFO.toLowerCase(), PLIST_ROOT.toLowerCase(), PLIST_ENTS.toLowerCase()
    ));

    private ReprofilingUtil() {}

    private static boolean hasOnlySupportedProfiles(String targetEnv, List<Profile> profiles) {
        boolean result = true;
        for (Profile profile : profiles) {
            if (targetEnv.equalsIgnoreCase(profile.getEnvironment()) && "artifact".equals(profile.getScope())) {
                if (profile.getTarget() == null || !updatableTargets.contains(profile.getTarget().toLowerCase())) {
                    result = false;
                    break;
                }
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    private static void logProfiles(List<Profile> profiles) {
        logger.info("Profiles filtered for target environment: {}", profiles.size());
        if (!profiles.isEmpty()) {
            for (Profile profile : profiles) {
                logger.info("  {}", profile);
            }
        }
    }

    public static void reprofileIpa(Project project, File targetIpa, String targetEnv, List<Profile> profiles, boolean cleanReleaseVersionForPROD) throws IOException {
        logger.info("Reprofiling IPA '{}' to environment '{}'", targetIpa.getAbsolutePath(), targetEnv);
        logProfiles(profiles);
        File workingDir = targetIpa.getParentFile();
        File payloadDir = new File(workingDir, "temp_ipa_payload");
        if (!targetIpa.exists()) {
            throw new IOException("Resign target '"+targetIpa.getAbsolutePath()+"' was not found");
        }
        if (!hasOnlySupportedProfiles(targetEnv, profiles)) {
            throw new IOException("Resign environment '"+targetIpa.getAbsolutePath()+"' has profiles incompatible with artifact resigning");
        }
        CommonUtil.unpackIpaPayload(targetIpa, payloadDir);
        File appDir = CommonUtil.getIpaPayloadApp(payloadDir);
        for (Profile profile : profiles) {
            File targetFile = new File(appDir, profile.getTarget());
            File profileFile = new File(profile.getSource());
            if (!profileFile.exists()) {
                throw new IOException("Profile file was not found");
            }
            if (PLIST_ENTS.equalsIgnoreCase(targetFile.getName())) {
                ProfilingUtil.profileFirstLevelPlistEntries(targetFile, profileFile, ProfilingUtilMode.UPDATE_AND_ADD);
            } else if (PLIST_ROOT.equalsIgnoreCase(profile.getTarget()) && profile.getLevel() == 2) {
                ProfilingUtil.profilePreferenceSpecifiersPlistEntries(targetFile, profileFile);
            } else {
                ProfilingUtil.profileFirstLevelPlistEntries(targetFile, profileFile);
            }
        }
        if (PropertyUtil.hasProjectProperty(project, "reprofiling.version")) {
            String version = PropertyUtil.getProjectProperty(project, "reprofiling.version");
            if (version.isEmpty()) {
                logger.info("Reprofiling version has not been provided. Will use the existing version.");
            } else {
                logger.info("Updating artifact version to '{}'", version);
                File infoPlist = new File(appDir, PLIST_INFO);
                File rootPlist = new File(appDir, PLIST_ROOT);
                PlistUtil.setStringValue(infoPlist, "CFBundleVersion", version);
                ProfilingUtil.updateRootPlistPreferenceSpecifiersKeyDefaultValue(rootPlist, "application_version", version);
                PlistUtil.resaveAsBinaryPlist(infoPlist);
                PlistUtil.validatePlist(infoPlist);
                PlistUtil.validatePlist(rootPlist);
            }
        } else {
            logger.info("Reprofiling version has not been provided. Will use the existing version.");
        }
        BackupUtil.applyChanges();
        Collection<File> fileCollection = FileUtils.listFiles(appDir, new String[] {"DS_Store"}, true);
        for (File dsStore : fileCollection) {
            FileUtils.forceDelete(dsStore);
        }
        IosCodesigningUtil.signApp(project, appDir);
        IosApp iosApp = new IosApp(appDir);
        BuildReportUtil.addIosAppInfo(iosApp);
        File resultIpa;
        if (targetIpa.getName().contains(" ")) {
            resultIpa = new File(targetIpa.getName().substring(0, targetIpa.getName().indexOf(' '))+' '+targetEnv+".ipa");
        } else {
            resultIpa = new File(targetIpa.getName().substring(0, targetIpa.getName().indexOf(".ipa"))+' '+targetEnv+".ipa");
        }
        CommonUtil.repackIpaPayload(payloadDir, resultIpa);
    }

}
