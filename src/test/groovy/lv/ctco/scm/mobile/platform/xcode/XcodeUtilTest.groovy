/*
 * @(#)XcodeUtilTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import org.junit.Test

import static org.junit.Assert.assertEquals

class XcodeUtilTest {

    @Test
    void parseXcodebuildListOutputClean() {
        List<String> testOutput = new ArrayList<>();
        testOutput.add("Information about project \"OBGYN\":");
        testOutput.add("    Targets:");
        testOutput.add("        OBGYN");
        testOutput.add("        OBGYN DEV");
        testOutput.add("        OBGYN TRAIN");
        testOutput.add("        OBGYN UAT");
        testOutput.add("");
        testOutput.add("    Build Configurations:");
        testOutput.add("        Debug");
        testOutput.add("        Release");
        testOutput.add("");
        testOutput.add("    If no build configuration is specified and -scheme is not passed then \"Release\" is used.");
        testOutput.add("");
        testOutput.add("    This project contains no schemes.");

        // TODO : Set up mock for parser testing
        XcodeUtil xcode = new XcodeUtil()
        xcode.xcodebuildListOutput = testOutput
        xcode.getGlobalConfiguration()

        List<String> targets = xcode.getTargets()
        assertEquals targets.size(), 4
        assertEquals targets[0], 'OBGYN'
        assertEquals targets[1], 'OBGYN DEV'
        assertEquals targets[2], 'OBGYN TRAIN'
        assertEquals targets[3], 'OBGYN UAT'

        List<String> buildConfigurations = xcode.getConfigurations()
        assertEquals buildConfigurations.size(), 2
        assertEquals buildConfigurations[0], 'Debug'
        assertEquals buildConfigurations[1], 'Release'
    }

    @Test
    void parseXcodebuildListOutputWithWarnings() {
        List<String> testOutput = new ArrayList<>();
        testOutput.add("Xcode warning about missing log files or source control plugin!");
        testOutput.add("Information about project \"OBGYN\":");
        testOutput.add("    Targets:");
        testOutput.add("        OBGYN");
        testOutput.add("        OBGYN DEV");
        testOutput.add("        OBGYN TRAIN");
        testOutput.add("        OBGYN UAT");
        testOutput.add("");
        testOutput.add("    Build Configurations:");
        testOutput.add("        Debug");
        testOutput.add("        Release");
        testOutput.add("");
        testOutput.add("    If no build configuration is specified and -scheme is not passed then \"Release\" is used.");
        testOutput.add("");
        testOutput.add("    This project contains no schemes.");

        // TODO : Set up mock for parser testing
        XcodeUtil xcode = new XcodeUtil()
        xcode.xcodebuildListOutput = testOutput
        xcode.getGlobalConfiguration()

        List<String> targets = xcode.getTargets()
        assertEquals targets.size(), 4
        assertEquals targets[0], 'OBGYN'
        assertEquals targets[1], 'OBGYN DEV'
        assertEquals targets[2], 'OBGYN TRAIN'
        assertEquals targets[3], 'OBGYN UAT'

        List<String> buildConfigurations = xcode.getConfigurations()
        assertEquals buildConfigurations.size(), 2
        assertEquals buildConfigurations[0], 'Debug'
        assertEquals buildConfigurations[1], 'Release'
    }

    @Test
    void parseXcodebuildShowbuildsettingsOutputClean() {
        List<String> testOutput = new ArrayList<>();
        testOutput.add("Build settings for action build and target iGREW:");
        testOutput.add("    xcodebuild[79139:1207] [MT] DeveloperPortal: Using pre-existing current store at URL (file://localhost/Users/techctco/Library/Developer/Xcode/DeveloperPortal%205.0.db).");
        testOutput.add("    ACTION = build");
        testOutput.add("    AD_HOC_CODE_SIGNING_ALLOWED = NO");
        testOutput.add("    ALTERNATE_GROUP = staff");
        testOutput.add("    ALTERNATE_MODE = u+w,go-w,a+rX");
        testOutput.add("    ALTERNATE_OWNER = user");
        testOutput.add("    EMPTY_PROPERTY =");

        Map<String, String> buildSettings = new XcodeUtil().parseBuildSettings(testOutput);

        assertEquals(buildSettings.size(), 6);
        assertEquals(buildSettings['ACTION'], 'build');
        assertEquals(buildSettings['AD_HOC_CODE_SIGNING_ALLOWED'], 'NO');
        assertEquals(buildSettings['ALTERNATE_GROUP'], 'staff');
        assertEquals(buildSettings['ALTERNATE_MODE'], 'u+w,go-w,a+rX');
        assertEquals(buildSettings['ALTERNATE_OWNER'], 'user');
        assertEquals(buildSettings['EMPTY_PROPERTY'], '');
    }

    @Test
    void parseXcodebuildShowbuildsettingsOutputWarnings() {
        List<String> testOutput = new ArrayList<>();
        testOutput.add("Xcode warning about missing log files or source control plugin!");
        testOutput.add("Build settings for action build and target iGREW:");
        testOutput.add("    xcodebuild[79139:1207] [MT] DeveloperPortal: Using pre-existing current store at URL (file://localhost/Users/techctco/Library/Developer/Xcode/DeveloperPortal%205.0.db).");
        testOutput.add("    ACTION = build");
        testOutput.add("    AD_HOC_CODE_SIGNING_ALLOWED = NO");
        testOutput.add("    ALTERNATE_GROUP = staff");
        testOutput.add("    ALTERNATE_MODE = u+w,go-w,a+rX");
        testOutput.add("    ALTERNATE_OWNER = user");
        testOutput.add("    EMPTY_PROPERTY =");

        Map<String, String> buildSettings = new XcodeUtil().parseBuildSettings(testOutput);

        assertEquals(buildSettings.size(), 6);
        assertEquals(buildSettings['ACTION'], 'build');
        assertEquals(buildSettings['AD_HOC_CODE_SIGNING_ALLOWED'], 'NO');
        assertEquals(buildSettings['ALTERNATE_GROUP'], 'staff');
        assertEquals(buildSettings['ALTERNATE_MODE'], 'u+w,go-w,a+rX');
        assertEquals(buildSettings['ALTERNATE_OWNER'], 'user');
        assertEquals(buildSettings['EMPTY_PROPERTY'], '');
    }

}
