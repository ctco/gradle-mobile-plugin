package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.CapturingOutputStream;
import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;

public class AndroidApksignerUtil {

    private static final Logger logger = Logging.getLogger(AndroidApksignerUtil.class);

    private static final String EXECUTABLE_APKSIGNER = "apksigner";

    private static final String COMMAND_SIGN = "sign";
    private static final String COMMAND_VERIFY = "verify";

    private static final String OPTION_VERBOSE = "--verbose";
    private static final String OPTION_PRINT_CERTS = "--print-certs";

    private static final String OPTION_KEYSTORE_FILE = "--ks";
    private static final String OPTION_KEYSTORE_PASS = "--ks-pass";
    private static final String OPTION_KEY_ALIAS = "--ks-key-alias";
    private static final String OPTION_KEY_PASS = "--key-pass";

    private AndroidApksignerUtil() {}

    private static File getCodesignExecutable() throws IOException {
        return new File(AndroidSDKUtil.getBuildToolsPath(), EXECUTABLE_APKSIGNER);
    }

    public static ExecResult sign(File apkFile, File keystoreFile, String keystorePass, String keyAlias, String keyPass) throws IOException {
        ExecCommand execCommand = new ExecCommand(getCodesignExecutable().getCanonicalPath());
        execCommand.addArgument(COMMAND_SIGN);
        execCommand.addArgument(OPTION_VERBOSE);
        execCommand.addArgument(OPTION_KEYSTORE_FILE);
        execCommand.addArgument(keystoreFile.getCanonicalPath(), false);
        execCommand.addArgument(OPTION_KEYSTORE_PASS);
        execCommand.addArgument("pass:"+keystorePass);
        execCommand.addArgument(OPTION_KEY_ALIAS);
        execCommand.addArgument(keyAlias);
        execCommand.addArgument(OPTION_KEY_PASS);
        execCommand.addArgument("pass:"+keyPass);
        execCommand.addArgument(apkFile.getCanonicalPath(), false);
        logger.debug("{}", execCommand);
        return ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
    }

    public static ExecResult verify(File apkFile) throws IOException {
        ExecCommand execCommand = new ExecCommand(getCodesignExecutable().getCanonicalPath());
        execCommand.addArgument(COMMAND_VERIFY);
        execCommand.addArgument(OPTION_VERBOSE);
        execCommand.addArgument(OPTION_PRINT_CERTS);
        execCommand.addArgument(apkFile.getCanonicalPath(), false);
        logger.debug("{}", execCommand);
        return ExecUtil.executeCommand(execCommand, new CapturingOutputStream());
    }

}
