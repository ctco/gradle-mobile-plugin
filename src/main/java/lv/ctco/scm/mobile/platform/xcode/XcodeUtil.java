/*
 * @(#)XcodeUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class XcodeUtil {

    private static List<String> buildTargets;
    private static List<String> buildConfigurations;

    private static List<String> xcodebuildListOutput;

    private XcodeUtil() {}

    public static List<String> getTargets() throws IOException {
        if (buildTargets == null) {
            getGlobalConfiguration();
        }
        return buildTargets;
    }

    private static void setTargets(List<String> targets) {
        buildTargets = targets;
    }

    public static String getDefaultTarget() throws IOException {
        String defaultTarget;
        List<String> targets = getTargets();
        if (targets == null || getTargets().isEmpty()) {
            throw new IOException("Could not get default Xcode target!");
        } else {
            defaultTarget = getTargets().get(0);
        }
        return defaultTarget;
    }

    public static List<String> getConfigurations() throws IOException {
        if (buildConfigurations == null) {
            getGlobalConfiguration();
        }
        return buildConfigurations;
    }

    public static List<String> getXcodebuildListOutput() {
        if (xcodebuildListOutput == null) {
            CommandLine commandLine = new CommandLine("xcodebuild");
            commandLine.addArgument("-list");
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
            if (execResult.isSuccess()) {
                xcodebuildListOutput = execResult.getOutput();
            }
        }
        return xcodebuildListOutput;
    }

    private static String getListOfStringsAsString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : list) {
            stringBuilder.append(line).append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    protected static void getGlobalConfiguration() throws IOException {
        buildTargets = new ArrayList<>();
        buildConfigurations = new ArrayList<>();
        String commandOutput = getListOfStringsAsString(getXcodebuildListOutput());
        Pattern xcodebuildListPattern = Pattern.compile(".*Information about project \"(.+)\":$.{1,2}^" +
                "    Targets:$.{1,2}^(.+)" +
                "    Build Configurations:$.{1,2}^(.+)" +
                "    If no build configuration is specified and -scheme is not passed then (\".+\") is used\\..*",
                Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = xcodebuildListPattern.matcher(commandOutput);
        if (m.matches()) {
            for (String item : m.group(2).split(System.lineSeparator())) {
                if (item.trim().length() > 0) {
                    buildTargets.add(item.trim());
                }
            }
            for (String item : m.group(3).split(System.lineSeparator())) {
                if (item.trim().length() > 0) {
                    buildConfigurations.add(item.trim());
                }
            }
        } else {
            throw new IOException("Unable to parse xcodebuild list command output");
        }
    }

    public static String getXcodeLibraryPublishRepoType(String libraryVersion) {
        String type = "";
        if (libraryVersion != null) {
            if (libraryVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                type = "-snapshots";
            } else {
                type = "-releases";
            }
        }
        return type;
    }

    static Map<String, String> getBuildSettings() throws IOException {
        return getBuildSettings(null);
    }

    static Map<String, String> getBuildSettings(String targetName) throws IOException {
        CommandLine commandLine = new CommandLine("xcodebuild");
        commandLine.addArgument("-showBuildSettings");
        if (targetName != null) {
            commandLine.addArgument("-target");
            commandLine.addArgument(targetName, false);
        }
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        return parseBuildSettings(execResult.getOutput());
    }

    protected static Map<String, String> parseBuildSettings(List<String> commandOutput) throws IOException {
        Pattern headingPattern = Pattern.compile("Build settings for action \\S+ and target .*");
        Pattern propertyPattern = Pattern.compile("\\s*(\\S+) = ?+(.*)");
        Map<String, String> properties = new HashMap<>();
        boolean headingFound = false;
        for (String line : commandOutput) {
            if (headingFound) {
                Matcher matcher = propertyPattern.matcher(line);
                if (matcher.matches()) {
                    properties.put(matcher.group(1), matcher.group(2));
                }
            } else {
                Matcher heading = headingPattern.matcher(line);
                if (heading.matches()) {
                    headingFound = true;
                }
            }
        }
        if (properties.size() == 0) {
            throw new IOException("Failed to get any xcodebuild settings!");
        }
        return properties;
    }

    static String getProductType(String targetName) throws IOException {
        Map<String, String> buildSettings = getBuildSettings(targetName);
        String productTypeValue = buildSettings.get("PRODUCT_TYPE");
        if (productTypeValue == null) {
            return "";
        } else {
            return productTypeValue;
        }
    }

}
