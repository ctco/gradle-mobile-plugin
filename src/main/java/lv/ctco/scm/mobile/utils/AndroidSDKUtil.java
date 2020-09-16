package lv.ctco.scm.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class AndroidSDKUtil {

    private static final String DEFAULT_BUILD_TOOLS_VERSION = "28.0.3";

    private AndroidSDKUtil() {}

    public static File getBuildToolsPath() throws IOException {
        String androidSdkRoot = System.getenv("ANDROID_SDK_ROOT");
        if (androidSdkRoot == null) {
            throw new IOException("Environment variable ANDROID_SDK_ROOT could not be found");
        }
        File platformTools = Paths.get(androidSdkRoot, "build-tools", DEFAULT_BUILD_TOOLS_VERSION).toFile();
        if (platformTools.exists()) {
            return platformTools;
        } else {
            throw new IOException("Android SDK build-tools/" + DEFAULT_BUILD_TOOLS_VERSION + " could not be found");
        }
    }

}
