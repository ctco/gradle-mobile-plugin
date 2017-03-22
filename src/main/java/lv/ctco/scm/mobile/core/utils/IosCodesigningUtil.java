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
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IosCodesigningUtil {

    private static final Logger logger = Logging.getLogger(IosCodesigningUtil.class);

    private static final String EXECUTABLE_CODESIGN = "codesign";

    private static final String COMMAND_DISPLAY = "--display";
    private static final String COMMAND_SIGN = "--sign";
    private static final String COMMAND_VERIFY = "--verify";

    private static final String OPTION_FORCE = "--force";
    private static final String OPTION_VERBOSE_2 = "--verbose=2";
    private static final String OPTION_VERBOSE_4 = "--verbose=4";

    private static final String OPTION_EXPLICIT_REQUIREMENT = "-R='anchor apple generic and certificate 1[field.1.2.840.113635.100.6.2.1] exists and (certificate leaf[field.1.2.840.113635.100.6.1.2] exists or certificate leaf[field.1.2.840.113635.100.6.1.4] exists)'";

    private IosCodesigningUtil() {}

    public static IosCodesigningIdentity getCodesigningIdentity(File appDir) {
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{COMMAND_DISPLAY, OPTION_VERBOSE_4, appDir.getAbsolutePath()}, false);
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

    public static List<String> getSignatureInformation(File file) {
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{COMMAND_DISPLAY, OPTION_VERBOSE_2, file.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        return execResult.getOutput();
    }

    private static void codesignApp(File appDir, String identity, File provisioning, File entitlements) throws IOException {
        FileUtils.copyFile(provisioning, new File(appDir, "embedded.mobileprovision"));
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{"-f", "-s", identity, OPTION_VERBOSE_4}, false);
        if (entitlements != null) {
            commandLine.addArguments(new String[]{"--entitlements", entitlements.getAbsolutePath()}, false);
        }
        commandLine.addArgument(appDir.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

    public static void verifyApp(File appDir) throws IOException {
        // --no-strict is required because of codesign "bug" in OSX 10.9.5 and newer; it bypasses obsolete envelope error.
        // Only include signed code in directories that should contain signed code.
        // Only include resources in directories that should contain resources.
        // Do not use the --resource-rules flag or ResourceRules.plist. They have been obsoleted and will be rejected.
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{COMMAND_VERIFY, "--deep", "--no-strict", OPTION_VERBOSE_4, appDir.getAbsolutePath()}, false);
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
            FileUtils.deleteDirectory(codeSignatureDir);
        }
        File embeddedProvisioning = getEmbeddedProvisioningFile(appDir);
        if (embeddedProvisioning != null && embeddedProvisioning.exists()) {
            FileUtils.forceDelete(embeddedProvisioning);
        }
    }

    private static void codesignFramework(File binary, String identity) throws IOException {
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{OPTION_FORCE, COMMAND_SIGN, identity, OPTION_VERBOSE_2, binary.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    private static void verifyFramework(File binary) throws IOException {
        CommandLine commandLine = new CommandLine(EXECUTABLE_CODESIGN);
        commandLine.addArguments(new String[]{COMMAND_VERIFY, OPTION_VERBOSE_4, binary.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException().getMessage());
        }
    }

    public static File signApp(File appDir, String identity, File provisioning) throws IOException {
        removeCurrentAppSignature(appDir);
        for (File framework : getFrameworks(appDir)) {
            codesignFramework(framework, identity);
            if (logger.isDebugEnabled()) {
                for (String line : getSignatureInformation(framework)) {
                    logger.debug(line);
                }
            }
            verifyFramework(framework);
        }
        for (File dylib : getDynamicLibraries(appDir)) {
            codesignFramework(dylib, identity);
            if (logger.isDebugEnabled()) {
                for (String line : getSignatureInformation(dylib)) {
                    logger.debug(line);
                }
            }
            verifyFramework(dylib);
        }
        codesignApp(appDir, identity, provisioning, getEntitlements(appDir));
        getSignatureInformation(appDir);
        verifyApp(appDir);
        return appDir;
    }

    public static File signApp(Project project, File appDir) throws IOException {
        //
        IosProvisioningProfile provisioning;
        String identity;

        if (PropertyUtil.hasProjectProperty(project, "signing.provisioning")) {
            String providedProvisioning = PropertyUtil.getProjectProperty(project, "signing.provisioning");
            logger.info("Will use provided provisioning profile value '{}'", providedProvisioning);
            if (providedProvisioning.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                provisioning = IosProvisioningUtil.getProvisioningProfileByUuid(providedProvisioning);
            } else {
                IosProvisioningUtil.getAvailableProvisioningProfiles();
                provisioning = IosProvisioningUtil.getProvisioningProfileByProfileName(providedProvisioning);
            }
        } else {
            logger.info("Will use automatically detected provisioning profile...");
            throw new IOException("Functionality is not yet supported! Please explicitly provide signing provisioning to use.");
        }
        if (provisioning == null) {
            throw new IOException("Provisioning profile to use for signing was not found!");
        } else if (provisioning.isExpired()) {
            throw new IOException("Provisioning profile to use for signing is expired!");
        } else {
            logger.info("Will use found provisioning profile '{}'", provisioning);
        }

        if (PropertyUtil.hasProjectProperty(project, "signing.identity")) {
            identity = PropertyUtil.getProjectProperty(project, "signing.identity");
            logger.info("Will use provided identity '{}'...", identity);
        } else {
            logger.info("Will use automatically detected identity from provisioning profile...");
            throw new IOException("Functionality is not yet supported! Please explicitly provide signing identity to use.");
        }
        if (identity == null || identity.isEmpty()) {
            throw new IOException("Signing identity to use is undefined!");
        }
        //
        signApp(appDir, identity, new File(provisioning.getLocation()));
        return appDir;
    }

    private static List<File> getFrameworks(File appDir) {
        List<File> frameworks = new ArrayList<>();
        File frameworkDir = new File(appDir, "Frameworks");
        if (frameworkDir.exists()) {
            Collection<File> files = FileUtils.listFilesAndDirs(frameworkDir,
                    FalseFileFilter.INSTANCE, FileFilterUtils.suffixFileFilter(".framework"));
            for (File file : files) {
                if (!file.equals(frameworkDir)) {
                    frameworks.add(file);
                }
            }
        }
        return frameworks;
    }

    private static List<File> getDynamicLibraries(File appDir) {
        List<File> dylibs = new ArrayList<>();
        File frameworkDir = new File(appDir, "Frameworks");
        if (frameworkDir.exists()) {
            Collection<File> files = FileUtils.listFiles(frameworkDir,
                    FileFilterUtils.suffixFileFilter(".dylib"), null);
            for (File file : files) {
                dylibs.add(file);
            }
        }
        return dylibs;
    }

}
