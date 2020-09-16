package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AndroidZipalignUtil {

    private static final String EXECUTABLE_ZIPALIGN = "zipalign";

    private static final String COMMAND_CHECK = "-c";

    private static final String DEFAULT_ALIGNMENT = "4";

    private static final String OPTION_VERBOSE = "-v";

    private AndroidZipalignUtil() {}

    private static File getZipalignExecutable() throws IOException {
        return new File(AndroidSDKUtil.getBuildToolsPath(), EXECUTABLE_ZIPALIGN);
    }

    public static void align(File apkFile) throws IOException {
        File zipalignedFile = new File(apkFile.getParentFile(), "zipaligned.apk");
        ExecCommand command = new ExecCommand(getZipalignExecutable().getCanonicalPath());
        command.addArguments(new String[]{DEFAULT_ALIGNMENT, apkFile.getCanonicalPath(), zipalignedFile.getCanonicalPath()}, false);
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (execResult.isFailure()) {
            Files.deleteIfExists(zipalignedFile.toPath());
            throw new IOException("Zipalign failed for apk file", execResult.getException());
        } else {
            Files.move(zipalignedFile.toPath(), apkFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void check(File apkFile) throws IOException {
        ExecCommand command = new ExecCommand(getZipalignExecutable().getCanonicalPath());
        command.addArgument(COMMAND_CHECK);
        command.addArgument(DEFAULT_ALIGNMENT);
        command.addArgument(apkFile.getCanonicalPath(), false);
        ExecResult execResult = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (execResult.isFailure()) {
            throw new IOException("Zipalign check failed for apk file", execResult.getException());
        }
    }

}
