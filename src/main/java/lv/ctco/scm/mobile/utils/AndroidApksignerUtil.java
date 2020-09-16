package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import java.io.File;
import java.io.IOException;

public class AndroidApksignerUtil {

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

    public static void sign(File apkFile, File keystoreFile, String keystorePass, String keyAlias, String keyPass) throws IOException {
        ExecCommand command = new ExecCommand(getCodesignExecutable().getCanonicalPath());
        command.addArgument(COMMAND_SIGN);
        command.addArgument(OPTION_VERBOSE);
        command.addArgument(OPTION_KEYSTORE_FILE);
        command.addArgument(keystoreFile.getCanonicalPath(), false);
        command.addArgument(OPTION_KEYSTORE_PASS);
        command.addArgument("pass:"+keystorePass);
        command.addArgument(OPTION_KEY_ALIAS);
        command.addArgument(keyAlias);
        command.addArgument(OPTION_KEY_PASS);
        command.addArgument("pass:"+keyPass);
        command.addArgument(apkFile.getCanonicalPath(), false);
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (execResult.isFailure()) {
            throw new IOException("Signing failed for apk file", execResult.getException());
        }
    }

    public static void verify(File apkFile) throws IOException {
        ExecCommand command = new ExecCommand(getCodesignExecutable().getCanonicalPath());
        command.addArgument(COMMAND_VERIFY);
        command.addArgument(OPTION_VERBOSE);
        command.addArgument(OPTION_PRINT_CERTS);
        command.addArgument(apkFile.getCanonicalPath(), false);
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (execResult.isFailure()) {
            throw new IOException("Signature verification failed for apk file", execResult.getException());
        }
    }

}
