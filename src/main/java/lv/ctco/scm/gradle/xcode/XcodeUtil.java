/*
 * @(#)XcodeUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.mobile.utils.ExecResult;
import lv.ctco.scm.mobile.utils.ExecUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public final class XcodeUtil {

    private static List<String> buildTargets;
    private static List<String> buildConfigurations;

    private static List<String> xcodebuildListOutput;
    private static File xcodeproj;

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

    private static void setXcodeproj(File file) {
        xcodeproj = file;
    }

    public static String getDefaultTarget() throws IOException {
        List<String> targets = getTargets();
        if (targets == null || getTargets().isEmpty()) {
            throw new IOException("Could not get default Xcode target!");
        } else {
            return getTargets().get(0);
        }
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
                item = item.trim();
                if (item.length() > 0) {
                    buildTargets.add(item);
                }
            }
            for (String item : m.group(3).split(System.lineSeparator())) {
                item = item.trim();
                if (item.length() > 0) {
                    buildConfigurations.add(item);
                }
            }
        } else {
            throw new IOException("Unable to parse xcodebuild list command output");
        }
    }

    public static Map<String, String> getBuildSettings() throws IOException {
        return getBuildSettings(null);
    }

    public static Map<String, String> getBuildSettings(String targetName) throws IOException {
        CommandLine commandLine = new CommandLine("xcodebuild");
        commandLine.addArgument("clean"); // A workaround for Xcode 8 bug
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
        if (properties.isEmpty()) {
            throw new IOException("Failed to get any xcodebuild settings!");
        }
        return properties;
    }

    public static String getProductType(String targetName) throws IOException {
        Map<String, String> buildSettings = getBuildSettings(targetName);
        String productTypeValue = buildSettings.get("PRODUCT_TYPE");
        if (productTypeValue == null) {
            return "";
        } else {
            return productTypeValue;
        }
    }

    public static int getXcodeprojCount(File dir) {
        if (xcodeproj == null) {
            List<File> files = getXcodeProjectFiles(dir);
            return files.size();
        } else {
            return 1;
        }
    }

    public static File getXcodeprojFile(File dir) {
        if (xcodeproj == null) {
            List<File> files = getXcodeProjectFiles(dir);
            return files.get(0);
        } else {
            return xcodeproj;
        }
    }

    private static List<File> getXcodeProjectFiles(File dir) {
        List<File> results = new ArrayList<>();
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isDirectory() && file.getParentFile().equals(dir)
                        && "xcodeproj".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                    results.add(file);
                }
            }
        }
        return results;
    }
    
    public static List<Environment> getAutodetectedEnvironments(String defaultTarget, List<String> allTargets) {
        List<Environment> environments = new ArrayList<>();
        Matcher defaultTargetMatcher = Pattern.compile("(\\w+) \\w+").matcher(defaultTarget);
        if (defaultTargetMatcher.matches()) {
            String prefix = defaultTargetMatcher.group(1);
            Pattern multitargetPattern = Pattern.compile(Pattern.quote(prefix)+" (\\w+)");
            for (String target : allTargets) {
                Matcher multitargetMatcher = multitargetPattern.matcher(target);
                if (multitargetMatcher.matches()) {
                    Environment env = new Environment();
                    env.setName(multitargetMatcher.group(1));
                    env.setTarget(target);
                    environments.add(env);
                }
            }
        } else {
            Environment env = new Environment();
            env.setName("DEFAULT");
            env.setTarget(defaultTarget);
            environments.add(env);
        }
        return environments;
    }

}
