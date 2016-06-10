/*
 * @(#)IosProvisioningUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import lv.ctco.scm.mobile.core.objects.IosProvisioningProfile;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Singleton
final class IosProvisioningUtil {

    private static HashMap<String, IosProvisioningProfile> provisioningProfiles = new HashMap<>();

    private IosProvisioningUtil() {}

    static void getAvailableProvisioningProfiles() throws IOException {
        File profileDir = PathUtil.getDefaultProvisioningProfileDir();
        provisioningProfiles.clear();
        int available = 0;
        int expired = 0;
        LoggerUtil.info("Reading available provisioning profiles...");
        for (File file : FileUtils.listFiles(profileDir, new String[] {"mobileprovision"}, false)) {
            IosProvisioningProfile profile = IosProvisioningUtil.getProvisioningProfileFromFile(file);
            if (profile.isExpired()) {
                LoggerUtil.debug("  expired '"+profile.toString()+"'");
                expired++;
            } else {
                LoggerUtil.debug("  valid '"+profile.toString()+"'");
                provisioningProfiles.put(profile.getUuid(), profile);
                available++;
            }
        }
        LoggerUtil.info("Found "+available+" provisioning profiles ("+expired+" of them are expired).");
    }

    static IosProvisioningProfile getProvisioningProfileFromFile(File profileFile) throws IOException {
        File plist = new File(PathUtil.getTempDir(), profileFile.getName()+".plist");
        convertProvisioningToPlist(profileFile, plist);
        //
        IosProvisioningProfile profile = new IosProvisioningProfile();
        profile.setUuid(PlistUtil.getStringValue(plist, "UUID"));
        profile.setProfileName(PlistUtil.getStringValue(plist, "Name"));
        profile.setTeamName(PlistUtil.getStringValue(plist, "TeamName"));
        profile.setExpirationDate(PlistUtil.getDateValue(plist, "ExpirationDate"));
        profile.setLocation(profileFile.getAbsolutePath());
        return profile;
    }

    private static File getProvisioningProfileFileByUuid(String uuid) {
        return new File(PathUtil.getDefaultProvisioningProfileDir(), uuid+".mobileprovision");
    }

    static IosProvisioningProfile getProvisioningProfileByUuid(String uuid) throws IOException {
        File profileFile = getProvisioningProfileFileByUuid(uuid);
        return getProvisioningProfileFromFile(profileFile);
    }

    static IosProvisioningProfile getProvisioningProfileByProfileName(String profileName) {
        for (IosProvisioningProfile profile : provisioningProfiles.values()) {
            if (profileName.equalsIgnoreCase(profile.getProfileName())) {
                if (profile.isExpired()) {
                    LoggerUtil.warn("Found an expired profile with name '"+profile.getProfileName()+"'");
                } else {
                    return profile;
                }
            }
        }
        return null;
    }

    private static void convertProvisioningToPlist(File profile, File plist) throws IOException {
        CommandLine commandLine = new CommandLine("security");
        commandLine.addArguments(new String[] {"cms", "-D", "-i", profile.getAbsolutePath(), "-o", plist.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

}
