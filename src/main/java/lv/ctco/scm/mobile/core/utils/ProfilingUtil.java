/*
 * @(#)ProfilingUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProfilingUtil {

    private static final Logger logger = Logging.getLogger(ProfilingUtil.class);

    private static final List<String> updatablePreferenceSpecifiers = new ArrayList<>(Arrays.asList(
            "PSMultiValueSpecifier",
            "PSRadioGroupSpecifier",
            "PSSliderSpecifier",
            "PSTextFieldSpecifier",
            "PSTitleValueSpecifier",
            "PSToggleSwitchSpecifier"
    ));

    private ProfilingUtil() {}

    public static void profileFirstLevelPlistEntries(File targetFile, File profileFile) throws IOException {
        profileFirstLevelPlistEntries(targetFile, profileFile, ProfilingUtilMode.UPDATE_EXISTING);
    }

    public static void profileFirstLevelPlistEntries(File targetFile, File profileFile, ProfilingUtilMode mode) throws IOException {
        logger.info("Profiling '"+targetFile.getAbsolutePath()+"'...");
        logger.info("  initial md5={}", CommonUtil.getMD5Hex(targetFile));
        BackupUtil.backupFile(targetFile);
        NSDictionary baseDict  = PlistUtil.getRootDictionary(targetFile);
        NSDictionary profDict  = PlistUtil.getRootDictionary(profileFile);
        NSDictionary rsltDict = new NSDictionary();
        for (Map.Entry<String, NSObject> baseEntry : baseDict.entrySet()) {
            if (profDict.containsKey(baseEntry.getKey())) {
                logger.info("  Replacing key '"+baseEntry.getKey()+"' with value '"+profDict.get(baseEntry.getKey())+"'");
                rsltDict.put(baseEntry.getKey(), profDict.get(baseEntry.getKey()));
            } else {
                rsltDict.put(baseEntry.getKey(), baseEntry.getValue());
            }
        }
        if (mode == ProfilingUtilMode.UPDATE_AND_ADD) {
            for (Map.Entry<String, NSObject> profEntry : profDict.entrySet()) {
                if (!rsltDict.containsKey(profEntry.getKey())) {
                    logger.info("  Adding key '"+profEntry.getKey()+"' with value '"+profDict.get(profEntry.getKey())+"'");
                    rsltDict.put(profEntry.getKey(), profEntry.getValue());
                }
            }
        }
        PropertyListParser.saveAsXML(rsltDict, targetFile.getAbsoluteFile());
        logger.info("  current md5={}", CommonUtil.getMD5Hex(targetFile));
    }

    public static void updateRootPlistPreferenceSpecifiersKeyDefaultValue(File plistFile, String keyToUpdate, String valueToSet) throws IOException {
        BackupUtil.backupFile(plistFile);
        NSDictionary rootDict  = PlistUtil.getRootDictionary(plistFile);
        boolean plistModified = false;
        for (Map.Entry<String, NSObject> subRootEntry : rootDict.entrySet()) {
            if ("PreferenceSpecifiers".equals(subRootEntry.getKey())
                    && subRootEntry.getValue() instanceof NSArray
                    && "com.dd.plist.NSArray".equals(subRootEntry.getValue().getClass().getCanonicalName())) {
                NSObject[] psArray = ((NSArray)subRootEntry.getValue()).getArray();
                NSDictionary psDict = (NSDictionary)psArray[0];
                for (Map.Entry<String, NSObject> psEntry : psDict.entrySet()) {
                    if (keyToUpdate.equals(psEntry.getKey())) {
                        psEntry.setValue(new NSString(valueToSet));
                        plistModified = true;
                        logger.info("  Replacing key '{}' with value '{}'", psEntry.getKey(), valueToSet);
                    }
                }
            }
        }
        if (plistModified) {
            PropertyListParser.saveAsXML(rootDict, plistFile.getAbsoluteFile());
            logger.info("  current md5={}", CommonUtil.getMD5Hex(plistFile));
        }
    }

    public static List<NSObject> getProfiledPreferenceSpecifiersObjectArray(List<NSObject> originalArray, List<NSObject> profileArray) {
        List<NSObject> resultArray = new ArrayList<>();
        for (NSObject originalObject : originalArray) {
            boolean targetProfiled = false;
            NSDictionary originalDict = (NSDictionary) originalObject;
            if (originalDict.get("Type") == null || updatablePreferenceSpecifiers.contains(originalDict.get("Type").toString())) {
                for (NSObject profileObject : profileArray) {
                    NSDictionary profileDict = (NSDictionary) profileObject;
                    if (originalDict.get("Key").equals(profileDict.get("Key"))) {
                        targetProfiled = true;
                        logger.info("  Replacing PreferenceSpecifiers key '"+profileDict.get("Key")+"'");
                        resultArray.add(profileObject);
                        break;
                    }
                }
            }
            if (!targetProfiled) {
                resultArray.add(originalObject);
            }
        }
        return resultArray;
    }

    public static List<NSObject> getPreferenceSpecifiersObjectArray(File rootPlist) throws IOException {
        NSDictionary rootDict  = PlistUtil.getRootDictionary(rootPlist);
        NSArray psValue = (NSArray) rootDict.get("PreferenceSpecifiers");
        if (psValue == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(psValue.getArray()));
        }
    }

    private static void updatePreferenceSpecifiersObjectArray(File rootPlist, List<NSObject> list) throws IOException {
        NSDictionary rootDict  = PlistUtil.getRootDictionary(rootPlist);
        NSObject value = getNSObjectFromArray(list.toArray());
        rootDict.put("PreferenceSpecifiers", value);
        PropertyListParser.saveAsXML(rootDict, rootPlist.getAbsoluteFile());
    }

    private static NSObject getNSObjectFromArray(Object[] object) {
        int size = Array.getLength(object);
        NSObject[] array = new NSObject[size];
        for(int i = 0; i < size; ++i) {
            array[i] = (NSObject)Array.get(object, i);
        }
        return new NSArray(array);
        //return NSObject.fromJavaObject(list.toArray()); //Convenience method introduced in a newer DDPlist version
    }

    public static void profilePreferenceSpecifiersPlistEntries(File target, File source) throws IOException {
        logger.info("Profiling '"+target.getAbsolutePath()+"'...");
        logger.info("  initial md5={}", CommonUtil.getMD5Hex(target));
        BackupUtil.backupFile(target);
        List<NSObject> targetArray = getPreferenceSpecifiersObjectArray(target);
        List<NSObject> sourceArray = getPreferenceSpecifiersObjectArray(source);
        List<NSObject> resultArray = getProfiledPreferenceSpecifiersObjectArray(targetArray, sourceArray);
        updatePreferenceSpecifiersObjectArray(target, resultArray);
        logger.info("  current md5={}", CommonUtil.getMD5Hex(target));
    }

}
