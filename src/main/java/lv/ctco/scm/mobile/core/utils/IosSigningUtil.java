/*
 * @(#)IosSigningUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import lv.ctco.scm.mobile.core.objects.IosCodesigningIdentity;
import lv.ctco.scm.mobile.core.objects.IosProvisioningProfile;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

public final class IosSigningUtil {

    private static final String COMMAND_CODESIGN = "codesign";
    private static final String OPTION_VERBOSE = "--verbose=4";

    private IosSigningUtil() {}

    public static IosCodesigningIdentity getCodesigningIdentity(File appDir) throws IOException {
        CommandLine commandLine = new CommandLine(COMMAND_CODESIGN);
        commandLine.addArguments(new String[]{"--display", OPTION_VERBOSE, appDir.getAbsolutePath()}, false);
        IosCodesigningIdentity iosCodesigningIdentity = new IosCodesigningIdentity();
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        if (execResult.isSuccess()) {
            for (String line : execResult.getOutput()) {
                if (line.trim().startsWith("Authority=")) {
                    iosCodesigningIdentity.setCommonName(line.replaceFirst("Authority=", ""));
                    if (iosCodesigningIdentity.getCommonName().startsWith("iPhone Distribution: ")) {
                        iosCodesigningIdentity.setIdentityType("iPhone Distribution");
                        iosCodesigningIdentity.setIdentityName(iosCodesigningIdentity.getCommonName().replace("iPhone Distribution: ", ""));
                    } else if (iosCodesigningIdentity.getCommonName().startsWith("iPhone Developer: ")) {
                        iosCodesigningIdentity.setIdentityType("iPhone Developer");
                        iosCodesigningIdentity.setIdentityName(iosCodesigningIdentity.getCommonName().replace("iPhone Developer: ", ""));
                    }
                    return iosCodesigningIdentity;
                }
            }
        }
        return null;
    }

    public static File getEmbeddedProvisioningFile(File appDir) {
        File provisioningFile = new File(appDir, "embedded.mobileprovision");
        if (provisioningFile.exists()) {
            return provisioningFile;
        } else {
            return null;
        }
    }

    public static IosProvisioningProfile getEmbeddedProvisioningProfile(File appDir) throws IOException {
        File provisioningFile = getEmbeddedProvisioningFile(appDir);
        if (provisioningFile != null && provisioningFile.exists()) {
            return IosProvisioningUtil.getProvisioningProfileFromFile(provisioningFile);
        } else {
            return null;
        }
    }

    public static void displayAppSignature(File appDir) {
        CommandLine commandLine = new CommandLine(COMMAND_CODESIGN);
        commandLine.addArguments(new String[]{"--display", OPTION_VERBOSE, appDir.getAbsolutePath()}, false);
        ExecUtil.execCommand(commandLine, null, null, false, true);
    }

    private static void codesignApp(File appDir, String identity, File provisioning, File entitlements) throws IOException {
        FileUtils.copyFile(provisioning, new File(appDir, "embedded.mobileprovision"));
        CommandLine commandLine = new CommandLine(COMMAND_CODESIGN);
        commandLine.addArguments(new String[]{"-f", "-s", identity, OPTION_VERBOSE, "--entitlements", entitlements.getAbsolutePath(), appDir.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

    public static void verifyApp(File appDir) throws IOException {
        // --no-strict is required because of codesign "bug" in OSX 10.9.5 and newer; it bypasses obsolete envelope error.
        CommandLine commandLine = new CommandLine(COMMAND_CODESIGN);
        commandLine.addArguments(new String[]{"--verify", "--deep", "--no-strict", OPTION_VERBOSE, appDir.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    private static File getEntitlements(File appDir) {
        for (File file : FileUtils.listFiles(appDir, new String[]{"entitlements", "xcent"}, false)) {
            if ("archived-expanded-entitlements.xcent".equals(file.getName())
                    || file.getName().toLowerCase().endsWith(".entitlements")) {
                return file.getAbsoluteFile();
            }
        }
        return null;
    }

    private static void removeCurrentAppSignature(File appDir) throws IOException {
        File codeSignatureDir = new File(appDir, "_CodeSignature");
        if (codeSignatureDir.exists()) {
            LoggerUtil.info("Removing existing signature...");
            FileUtils.deleteDirectory(codeSignatureDir);
        }
        File embeddedProvisioning = getEmbeddedProvisioningFile(appDir);
        if (embeddedProvisioning != null && embeddedProvisioning.exists()) {
            LoggerUtil.info("Removing existing embedded provisioning...");
            FileUtils.forceDelete(embeddedProvisioning);
        }
    }

    public static File signApp(File appDir, String identity, File provisioning) throws IOException {
        LoggerUtil.info("Signing '"+appDir.getAbsolutePath()+"'...");
        removeCurrentAppSignature(appDir);
        codesignApp(appDir, identity, provisioning, getEntitlements(appDir));
        displayAppSignature(appDir);
        verifyApp(appDir);
        return appDir;
    }

    public static File signApp(Project project, File appDir) throws IOException {
        LoggerUtil.info("Signing '"+appDir.getAbsolutePath()+"'...");
        //
        IosProvisioningProfile provisioning;
        String identity;

        if (PropertyUtil.hasProjectProperty(project, "signing.provisioning")) {
            String providedProvisioning = PropertyUtil.getProjectProperty(project, "signing.provisioning");
            LoggerUtil.info("Will use provided provisioning profile value '"+providedProvisioning+"'");
            if (providedProvisioning.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                provisioning = IosProvisioningUtil.getProvisioningProfileByUuid(providedProvisioning);
            } else {
                IosProvisioningUtil.getAvailableProvisioningProfiles();
                provisioning = IosProvisioningUtil.getProvisioningProfileByProfileName(providedProvisioning);
            }
        } else {
            LoggerUtil.info("Will use automatically detected provisioning profile...");
            throw new IOException("Functionality is not yet supported! Please explicitly provide signing provisioning to use.");
        }
        if (provisioning == null) {
            throw new IOException("Provisioning profile to use for signing was not found!");
        } else if (provisioning.isExpired()) {
            throw new IOException("Provisioning profile to use for signing is expired!");
        } else {
            LoggerUtil.info("Will use found provisioning profile '"+provisioning.toString()+"'");
        }

        if (PropertyUtil.hasProjectProperty(project, "signing.identity")) {
            identity = PropertyUtil.getProjectProperty(project, "signing.identity");
            LoggerUtil.info("Will use provided identity '"+identity+"'...");
        } else {
            LoggerUtil.info("Will use automatically detected identity from provisioning profile...");
            throw new IOException("Functionality is not yet supported! Please explicitly provide signing identity to use.");
        }
        if (identity == null || identity.isEmpty()) {
            throw new IOException("Signing identity to use is undefined!");
        }
        //
        signApp(appDir, identity, new File(provisioning.getLocation()));
        return appDir;
    }

}
