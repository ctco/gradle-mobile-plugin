/*
 * @(#)PlistUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import com.dd.plist.NSDate;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

import org.apache.commons.exec.CommandLine;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public final class PlistUtil {

    private static final Logger logger = Logging.getLogger(PlistUtil.class);

    private static final String EXCEPTION_MSG_PARSE = "Exception while parsing a plist file";

    private PlistUtil() {}

    public static String getStringValue(File plistFile, String keyName) throws IOException {
        NSDictionary baseDict = PlistUtil.getRootDictionary(plistFile);
        if (baseDict.containsKey(keyName)) {
            NSObject entry = baseDict.get(keyName);
            return entry.toString();
        } else {
            return null;
        }
    }

    public static Date getDateValue(File plistFile, String keyName) throws IOException {
        NSDictionary baseDict = PlistUtil.getRootDictionary(plistFile);
        if (baseDict.containsKey(keyName)) {
            NSObject entry = baseDict.get(keyName);
            return ((NSDate)entry).getDate();
        } else {
            return null;
        }
    }

    public static void setStringValue(File plistFile, String keyName, String keyValue) throws IOException {
        BackupUtil.backupFile(plistFile);
        NSDictionary baseDict = PlistUtil.getRootDictionary(plistFile);
        if (baseDict.containsKey(keyName)) {
            logger.info("  Replacing key '{}' with value '{}'", keyName, keyValue);
            baseDict.put(keyName, keyValue);
        } else {
            logger.info("  Adding key '{}' with value '{}'", keyName, keyValue);
            baseDict.put(keyName, keyValue);
        }
        PropertyListParser.saveAsXML(baseDict, plistFile);
        logger.info("  current md5={}", CommonUtil.getMD5Hex(plistFile));
    }

    public static void validatePlist(File plistFile) throws IOException {
        CommandLine commandLine = new CommandLine("plutil");
        commandLine.addArguments(new String[]{"-lint", "-s", plistFile.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

    public static void resaveAsBinaryPlist(File plist) throws IOException {
        PropertyListParser.saveAsBinary(getRootDictionary(plist), plist);
    }

    public static void resaveAsXmlPlist(File plist) throws IOException {
        PropertyListParser.saveAsXML(getRootDictionary(plist), plist);
    }

    public static NSDictionary getRootDictionary(File plist) throws IOException {
        NSDictionary rootDictionary;
        try {
            rootDictionary = (NSDictionary) PropertyListParser.parse(plist);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(EXCEPTION_MSG_PARSE+" - '"+plist.getAbsolutePath()+"'", e);
        }
        return rootDictionary;
    }

}
