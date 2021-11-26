/*
 * @(#)IosSigningUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.CapturingOutputStream;
import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IosCodesigningUtil {

    private static final Logger logger = Logging.getLogger(IosCodesigningUtil.class);

    private static final String EXECUTABLE_CODESIGN = "codesign";

    private static final String COMMAND_DISPLAY = "--display";
    private static final String COMMAND_SIGN = "--sign";
    private static final String COMMAND_VERIFY = "--verify";

    private static final String OPTION_FORCE = "--force";
    private static final String OPTION_VERBOSE_2 = "--verbose=2";
    private static final String OPTION_VERBOSE_4 = "--verbose=4";

    private IosCodesigningUtil() {}

    public static IosCodesigningIdentity getCodesigningIdentity(File appDir) {
        ExecCommand execCommand = new ExecCommand(EXECUTABLE_CODESIGN);
        execCommand.addArguments(new String[]{COMMAND_DISPLAY, OPTION_VERBOSE_4, appDir.getAbsolutePath()}, false);
        IosCodesigningIdentity iosCodesigningIdentity = new IosCodesigningIdentity();
        logger.debug("Executing command:{}", execCommand);
        ExecResult execResult = ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
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

    private static File getEmbeddedProvisioningFile(File appDir) {
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

    private static ExecResult signApp(File appDir, String identity, File provisioning, File entitlements) throws IOException {
        if (provisioning != null) {
            FileUtils.copyFile(provisioning, new File(appDir, "embedded.mobileprovision"));
        }
        ExecCommand execCommand = new ExecCommand(EXECUTABLE_CODESIGN);
        execCommand.addArguments(new String[]{OPTION_FORCE, COMMAND_SIGN, identity, OPTION_VERBOSE_4}, false);
        if (entitlements != null) {
            execCommand.addArguments(new String[]{"--entitlements", entitlements.getAbsolutePath()}, false);
        }
        execCommand.addArgument(appDir.getName(), false);
        execCommand.setWorkingDirectory(appDir.getParentFile());
        logger.debug("Executing command:{}", execCommand);
        ExecResult execResult = ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
        if (execResult.isFailure()) {
            throw new IOException(execResult.getException());
        }
        return execResult;
    }

    public static ExecResult verifyApp(File appDir) {
        // --no-strict is required because of codesign "bug" in OSX 10.9.5 and newer; it bypasses obsolete envelope error.
        // Only include signed code in directories that should contain signed code.
        // Only include resources in directories that should contain resources.
        // Do not use the --resource-rules flag or ResourceRules.plist. They have been obsoleted and will be rejected.
        ExecCommand execCommand = new ExecCommand(EXECUTABLE_CODESIGN);
        execCommand.setWorkingDirectory(appDir.getParentFile());
        execCommand.addArguments(new String[]{COMMAND_VERIFY, "--deep", "--no-strict", OPTION_VERBOSE_4, appDir.getName()}, false);
        logger.debug("Executing command:{}", execCommand);
        return ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
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

    public static ExecResult signApp(File appDir, String identity, File provisioning, boolean verify) throws IOException {
        List<String> output = new ArrayList<>();
        removeCurrentAppSignature(appDir);
        for (Path frameworkSignable : getFrameworkSignables(appDir)) {
            ExecResult sign = signFramework(frameworkSignable, identity);
            output.addAll(sign.getOutput());
            if (sign.isFailure()) {
                return new ExecResult(output, sign.getException());
            }
            if (verify) {
                ExecResult verifyFramework = verifyFramework(frameworkSignable);
                output.addAll(verifyFramework.getOutput());
                if (verifyFramework.isFailure()) {
                    return new ExecResult(output, verifyFramework.getException());
                }
            }
        }
        ExecResult signApp = signApp(appDir, identity, provisioning, getEntitlements(appDir));
        output.addAll(signApp.getOutput());
        if (verify) {
            ExecResult verifyApp = verifyApp(appDir);
            output.addAll(verifyApp.getOutput());
            if (verifyApp.isFailure()) {
                return new ExecResult(output, verifyApp.getException());
            }
        }
        return new ExecResult(output);
    }

    @Deprecated // (since = "0.15.1.0", forRemoval = true)
    public static ExecResult signApp(File appDir, String identity, File provisioning) throws IOException {
        return signApp(appDir, identity, provisioning, true);
    }

    private static List<Path> getFrameworkSignables(File appDir) throws IOException {
        List<Path> signables = new ArrayList<>();
        Path frameworkDir = new File(appDir, "Frameworks").getCanonicalFile().toPath();
        if (frameworkDir.toFile().exists()) {
            try (Stream<Path> stream = Files.list(frameworkDir)) {
                List<Path> frameworks = stream
                        .filter(Files::isDirectory)
                        .filter(path -> path.getFileName().toString().endsWith(".framework"))
                        .collect(Collectors.toList());
                signables.addAll(frameworks);
            }
            try (Stream<Path> stream = Files.list(frameworkDir)) {
                List<Path> dylibs = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".dylib"))
                        .collect(Collectors.toList());
                signables.addAll(dylibs);
            }
        }
        return signables;
    }

    private static ExecResult signFramework(Path framework, String identity) {
        ExecCommand execCommand = new ExecCommand(EXECUTABLE_CODESIGN);
        execCommand.setWorkingDirectory(framework.getParent().toFile());
        execCommand.addArguments(new String[]{OPTION_FORCE, COMMAND_SIGN, identity, OPTION_VERBOSE_2, framework.getFileName().toString()}, false);
        logger.debug("Executing command:{}", execCommand);
        return ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
    }

    private static ExecResult verifyFramework(Path framework) {
        ExecCommand execCommand = new ExecCommand(EXECUTABLE_CODESIGN);
        execCommand.setWorkingDirectory(framework.getParent().toFile());
        execCommand.addArguments(new String[]{COMMAND_VERIFY, OPTION_VERBOSE_4, framework.getFileName().toString()}, false);
        logger.debug("Executing command:{}", execCommand);
        return ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
    }

}
