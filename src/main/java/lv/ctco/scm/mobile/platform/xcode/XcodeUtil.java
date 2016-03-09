/*
 * @(#)XcodeUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;

import org.gradle.api.GradleException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XcodeUtil {

    private List<String> buildTargets;
    private List<String> buildConfigurations;

    private List<String> xcodebuildListOutput;

    public List<String> getTargets() throws IOException {
        if (buildTargets == null) {
            getGlobalConfiguration();
        }
        return buildTargets;
    }

    public String getDefaultTarget() throws IOException {
        String defaultTarget;
        List<String> targets = getTargets();
        if (targets == null || getTargets().isEmpty()) {
            throw new GradleException("Could not get default Xcode target!");
        } else {
            defaultTarget = getTargets().get(0);
        }
        return defaultTarget;
    }

    public List<String> getConfigurations() throws IOException {
        if (buildConfigurations == null) {
            getGlobalConfiguration();
        }
        return buildConfigurations;
    }

    public List<String> getXcodebuildListOutput() {
        if (xcodebuildListOutput == null) {
            CommandLine commandLine = new CommandLine("xcodebuild");
            commandLine.addArgument("-list");
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
            if (execResult.isSuccess()) {
                return execResult.getOutput();
            }
        }
        return xcodebuildListOutput;
    }

    private static String getListOfStringsAsString(List<String> list) {
        String result = "";
        for (String line : list) {
            result += line + System.lineSeparator();
        }
        return result;
    }

    protected void getGlobalConfiguration() throws IOException {
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

    public String getXcodeLibraryPublishRepoType(String libraryVersion) {
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

    public Map<String, String> getBuildSettings() {
        return getBuildSettings(null);
    }

    public Map<String, String> getBuildSettings(String targetName) {
        CommandLine commandLine = new CommandLine("xcodebuild");
        commandLine.addArgument("-showBuildSettings");
        if (targetName != null) {
            commandLine.addArgument("-target");
            commandLine.addArgument(targetName, false);
        }
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        return parseBuildSettings(execResult.getOutput());
    }

    protected Map<String, String> parseBuildSettings(List<String> commandOutput) {
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
            throw new GradleException("Failed to get any xcodebuild settings!");
        }
        return properties;
    }

    public String getProductType(String targetName) {
        Map<String, String> buildSettings = getBuildSettings(targetName);
        String productTypeValue = buildSettings.get("PRODUCT_TYPE");
        if (productTypeValue == null) {
            return "";
        } else {
            return productTypeValue;
        }
    }

}
