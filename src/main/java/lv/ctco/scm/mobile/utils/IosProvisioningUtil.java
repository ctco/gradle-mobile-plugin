/*
 * @(#)IosProvisioningUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.LoggerOutputStream;

import lv.ctco.scm.utils.file.FileUtil;
import org.apache.commons.io.FileUtils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

final class IosProvisioningUtil {

    private static final Logger logger = Logging.getLogger(IosProvisioningUtil.class);

    private static HashMap<String, IosProvisioningProfile> provisioningProfiles = new HashMap<>();

    private IosProvisioningUtil() {}

    private static File getDefaultProvisioningProfileDir() {
        return new File(FileUtils.getUserDirectory(), "Library/MobileDevice/Provisioning Profiles");
    }

    static void getAvailableProvisioningProfiles() throws IOException {
        File profileDir = getDefaultProvisioningProfileDir();
        provisioningProfiles.clear();
        int available = 0;
        int expired = 0;
        logger.info("Reading available provisioning profiles...");
        for (File file : FileUtils.listFiles(profileDir, new String[] {"mobileprovision"}, false)) {
            IosProvisioningProfile profile = IosProvisioningUtil.getProvisioningProfileFromFile(file);
            if (profile.isExpired()) {
                logger.debug("  expired '{}'", profile);
                expired++;
            } else {
                logger.debug("  valid '{}'", profile);
                provisioningProfiles.put(profile.getUuid(), profile);
                available++;
            }
        }
        logger.info("Found {} provisioning profiles ({} of them are expired).", available, expired);
    }

    static IosProvisioningProfile getProvisioningProfileFromFile(File profileFile) throws IOException {
        File tempDir = Files.createTempDirectory("").toFile().getAbsoluteFile();
        File plist = new File(tempDir, profileFile.getName()+".plist");
        convertProvisioningToPlist(profileFile, plist);
        //
        IosProvisioningProfile profile = new IosProvisioningProfile();
        profile.setUuid(PlistUtil.getStringValue(plist, "UUID"));
        profile.setProfileName(PlistUtil.getStringValue(plist, "Name"));
        profile.setTeamName(PlistUtil.getStringValue(plist, "TeamName"));
        profile.setExpirationDate(PlistUtil.getDateValue(plist, "ExpirationDate"));
        profile.setLocation(profileFile.getAbsolutePath());
        //
        FileUtil.cleanDirectory(tempDir);
        return profile;
    }

    private static File getProvisioningProfileFileByUuid(String uuid) {
        return new File(getDefaultProvisioningProfileDir(), uuid+".mobileprovision");
    }

    static IosProvisioningProfile getProvisioningProfileByUuid(String uuid) throws IOException {
        File profileFile = getProvisioningProfileFileByUuid(uuid);
        return getProvisioningProfileFromFile(profileFile);
    }

    static IosProvisioningProfile getProvisioningProfileByProfileName(String profileName) {
        for (IosProvisioningProfile profile : provisioningProfiles.values()) {
            if (profileName.equalsIgnoreCase(profile.getProfileName())) {
                return profile;
            }
        }
        return null;
    }

    private static void convertProvisioningToPlist(File profile, File plist) throws IOException {
        ExecCommand execCommand = new ExecCommand("security");
        execCommand.addArguments(new String[] {"cms", "-D", "-i", profile.getAbsolutePath(), "-o", plist.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.executeCommand(execCommand, new LoggerOutputStream());
        if (execResult.isFailure()) {
            for (String line : execResult.getOutput()) {
                logger.info(line);
            }
            throw new IOException(execResult.getException());
        }
    }

}
