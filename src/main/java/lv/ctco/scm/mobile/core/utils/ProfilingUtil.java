/*
 * @(#)ProfilingUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import com.dd.plist.*;
import org.apache.commons.exec.CommandLine;
import org.xml.sax.SAXException;

import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@Singleton
public final class ProfilingUtil {

    private ProfilingUtil() {}

    public static void profileUsingPlistEntries(File targetFile, File profileFile) throws IOException {
        profileUsingPlistEntries(targetFile, profileFile, ProfilingUtilMode.UPDATE_EXISTING);
    }

    public static void profileUsingPlistEntries(File targetFile, File profileFile, ProfilingUtilMode mode) throws IOException {
        BackupUtil.backupFile(targetFile);
        NSDictionary baseDict;
        NSDictionary profDict;
        try {
            baseDict = (NSDictionary) PropertyListParser.parse(targetFile);
            profDict = (NSDictionary) PropertyListParser.parse(profileFile);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
        NSDictionary rsltDict = new NSDictionary();

        for (Map.Entry<String, NSObject> baseEntry : baseDict.entrySet()) {
            if (profDict.containsKey(baseEntry.getKey())) {
                LoggerUtil.info("Replacing node "+baseEntry.getKey()+" with value ("+profDict.get(baseEntry.getKey()).toString()+")");
                rsltDict.put(baseEntry.getKey(), profDict.get(baseEntry.getKey()));
            } else {
                rsltDict.put(baseEntry.getKey(), baseEntry.getValue());
            }
        }
        if (mode.equals(ProfilingUtilMode.UPDATE_AND_ADD)) {
            for (Map.Entry<String, NSObject> profEntry : profDict.entrySet()) {
                if (!rsltDict.containsKey(profEntry.getKey())) {
                    LoggerUtil.info("Adding node "+profEntry.getKey()+" with value ("+profDict.get(profEntry.getKey()).toString()+")");
                    rsltDict.put(profEntry.getKey(), profEntry.getValue());
                }
            }
        }

        PropertyListParser.saveAsXML(rsltDict, targetFile);
        LoggerUtil.info("Updated file "+CommonUtil.getMD5InfoString(targetFile));
    }

    public static void updateRootPlistPreferenceSpecifiersKeyDefaultValue(File plistFile, String keyToUpdate, String valueToSet) throws IOException {
        BackupUtil.backupFile(plistFile);
        NSDictionary rootDict;
        try {
            rootDict = (NSDictionary) PropertyListParser.parse(plistFile);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
        for (Map.Entry<String, NSObject> subRootEntry : rootDict.entrySet()) {
            if ("PreferenceSpecifiers".equals(subRootEntry.getKey())
                    && subRootEntry.getValue() instanceof NSArray
                    && "com.dd.plist.NSArray".equals(subRootEntry.getValue().getClass().getCanonicalName())) {
                NSObject[] psArray = ((NSArray)subRootEntry.getValue()).getArray();
                NSDictionary psDict = (NSDictionary)psArray[0];
                for (Map.Entry<String, NSObject> psEntry : psDict.entrySet()) {
                    if (keyToUpdate.equals(psEntry.getKey())) {
                        psEntry.setValue(new NSString(valueToSet));
                    }
                }
            }
        }
        PropertyListParser.saveAsXML(rootDict, plistFile);
    }

    /**
     * @deprecated Deprecated in favour of GroovyProfilingUtil.
     * @param target .
     * @param template .
     * @param environmentName .
     * @throws IOException .
     */
    @Deprecated
    public static void profileUsingT4Templates(File target, File template, String environmentName) throws IOException {
        LoggerUtil.warn("Project is using a deprecated profiling method. Migrate to GroovyProfiling.");
        File textTransformExecutable = PathUtil.getTextTransformExecutable();
        if (textTransformExecutable == null) {
            throw new IOException("Failed to find TextTransform executable!");
        }
        BackupUtil.backupFile(target);
        CommandLine commandLine = new CommandLine("mono");
        commandLine.addArguments(new String[] {textTransformExecutable.getAbsolutePath(), "-a=environmentName!"+environmentName, "--out="+target.getAbsolutePath(), template.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, new File("."), null, false, true);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

}
