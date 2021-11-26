/*
 * @(#)IosSigningUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IosCodesigningUtil {

    private static final String EXECUTABLE_CODESIGN = "codesign";

    private static final String COMMAND_DISPLAY = "--display";
    private static final String COMMAND_SIGN = "--sign";
    private static final String COMMAND_VERIFY = "--verify";

    private static final String OPTION_FORCE = "--force";
    private static final String OPTION_VERBOSE_2 = "--verbose=2";
    private static final String OPTION_VERBOSE_4 = "--verbose=4";

    private IosCodesigningUtil() {}

    public static IosCodesigningIdentity getCodesigningIdentity(File appDir) {
        ExecCommand command = new ExecCommand(EXECUTABLE_CODESIGN);
        command.addArguments(new String[]{COMMAND_DISPLAY, OPTION_VERBOSE_4, appDir.getAbsolutePath()}, false);
        IosCodesigningIdentity iosCodesigningIdentity = new IosCodesigningIdentity();
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
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
        ExecCommand command = new ExecCommand(EXECUTABLE_CODESIGN);
        command.addArguments(new String[]{OPTION_FORCE, COMMAND_SIGN, identity, OPTION_VERBOSE_4}, false);
        if (entitlements != null) {
            command.addArguments(new String[]{"--entitlements", entitlements.getAbsolutePath()}, false);
        }
        command.addArgument(appDir.getName(), false);
        command.setWorkingDirectory(appDir.getParentFile());
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
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
        ExecCommand command = new ExecCommand(EXECUTABLE_CODESIGN);
        command.setWorkingDirectory(appDir.getParentFile());
        command.addArguments(new String[]{COMMAND_VERIFY, "--deep", "--no-strict", OPTION_VERBOSE_4, appDir.getName()}, false);
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
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
        for (File framework : getFrameworks(appDir)) {
            ExecResult sign = signFramework(framework, identity);
            output.addAll(sign.getOutput());
            if (sign.isFailure()) {
                return new ExecResult(output, sign.getException());
            }
            if (verify) {
                ExecResult verifyFramework = verifyFramework(framework);
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

    /**
     * Deprecated since 0.15.1.0
     * Should be removed in 0.16.0.0
     * Method with the additional explicit boolean verify parameter should be used.
     */
    @Deprecated
    public static ExecResult signApp(File appDir, String identity, File provisioning) throws IOException {
        return signApp(appDir, identity, provisioning, true);
    }

    private static List<File> getFrameworks(File appDir) {
        List<File> frameworks = new ArrayList<>();
        File frameworkDir = new File(appDir, "Frameworks");
        if (frameworkDir.exists()) {
            Collection<File> files = FileUtils.listFilesAndDirs(frameworkDir, FalseFileFilter.INSTANCE, FileFilterUtils.suffixFileFilter(".framework"));
            for (File file : files) {
                if (!file.equals(frameworkDir)) {
                    frameworks.add(file);
                }
            }
            frameworks.addAll(FileUtils.listFiles(frameworkDir, FileFilterUtils.suffixFileFilter(".dylib"), null));
        }
        return frameworks;
    }

    private static ExecResult signFramework(File framework, String identity) {
        ExecCommand command = new ExecCommand(EXECUTABLE_CODESIGN);
        command.setWorkingDirectory(framework.getParentFile());
        command.addArguments(new String[]{OPTION_FORCE, COMMAND_SIGN, identity, OPTION_VERBOSE_2, framework.getName()}, false);
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    private static ExecResult verifyFramework(File framework) {
        ExecCommand command = new ExecCommand(EXECUTABLE_CODESIGN);
        command.setWorkingDirectory(framework.getParentFile());
        command.addArguments(new String[]{COMMAND_VERIFY, OPTION_VERBOSE_4, framework.getName()}, false);
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

}
