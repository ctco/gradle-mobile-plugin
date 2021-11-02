package lv.ctco.scm.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AndroidSDKUtil {

    private static final String BUILD_TOOLS = "build-tools";

    private AndroidSDKUtil() {}

    private static Path getBuildToolsRootPath() throws IOException {
        String androidSdkRoot = System.getenv("ANDROID_SDK_ROOT");
        if (androidSdkRoot == null) {
            throw new IOException("Environment variable ANDROID_SDK_ROOT could not be found");
        }
        Path buildToolsRoot = Paths.get(androidSdkRoot, BUILD_TOOLS);
        if (Files.exists(buildToolsRoot)) {
            return buildToolsRoot;
        } else {
            throw new IOException("Android SDK " + BUILD_TOOLS + " directory could not be found");
        }
    }

    private static File getLatestBuildToolsPath(Path buildToolsRoot) throws IOException {
        try (Stream<Path> stream = Files.list(buildToolsRoot)) {
            List<Path> versions = stream
                    .filter(Files::isDirectory)
                    .sorted()
                    .collect(Collectors.toList());
            versions.remove(buildToolsRoot);
            if (versions.isEmpty()) {
                throw new IOException("Android SDK " + BUILD_TOOLS + " directory contains no versions");
            }
            return versions.get(versions.size()-1).toFile();
        }
    }

    public static File getBuildToolsPath() throws IOException {
        Path buildToolsRoot = getBuildToolsRootPath();
        File buildTools = getLatestBuildToolsPath(buildToolsRoot);
        if (buildTools.exists()) {
            return buildTools;
        } else {
            throw new IOException("Android SDK " + BUILD_TOOLS + " directory contains no versions");
        }
    }

    public static File getBuildToolsPath(String buildToolsVersion) throws IOException {
        Path buildToolsRoot = getBuildToolsRootPath();
        File buildTools = new File(buildToolsRoot.toFile(), buildToolsVersion);
        if (buildTools.exists()) {
            return buildTools;
        } else {
            throw new IOException("Android SDK " + BUILD_TOOLS + "/" + buildToolsVersion + " directory could not be found");
        }
    }

}
