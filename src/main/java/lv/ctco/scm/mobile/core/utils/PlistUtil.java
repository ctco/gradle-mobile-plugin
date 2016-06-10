/*
 * @(#)PlistUtil.java
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
import java.util.Date;

@Singleton
public final class PlistUtil {

    private static final String EXCEPTION_MSG_PARSE = "Exception while parsing a plist file";

    private PlistUtil() {}

    public static String getStringValue(File plistFile, String keyName) throws IOException {
        NSDictionary baseDict;
        try {
            baseDict = (NSDictionary) PropertyListParser.parse(plistFile);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(EXCEPTION_MSG_PARSE, e);
        }
        if (baseDict.containsKey(keyName)) {
            NSObject entry = baseDict.get(keyName);
            return entry.toString();
        } else {
            return null;
        }
    }

    public static Date getDateValue(File plistFile, String keyName) throws IOException {
        NSDictionary baseDict;
        try {
            baseDict = (NSDictionary)PropertyListParser.parse(plistFile);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(EXCEPTION_MSG_PARSE, e);
        }
        if (baseDict.containsKey(keyName)) {
            NSObject entry = baseDict.get(keyName);
            return ((NSDate)entry).getDate();
        } else {
            return null;
        }
    }

    public static void setStringValue(File plistFile, String keyName, String keyValue) throws IOException {
        BackupUtil.backupFile(plistFile);
        NSDictionary baseDict;
        try {
            baseDict = (NSDictionary)PropertyListParser.parse(plistFile);
        } catch (PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            throw new IOException(EXCEPTION_MSG_PARSE, e);
        }
        if (baseDict.containsKey(keyName)) {
            LoggerUtil.info("Replacing node ["+keyName+"] with value ("+keyValue+")");
            baseDict.put(keyName, keyValue);
        } else {
            LoggerUtil.info("Adding node ["+keyName+"] with value ("+keyValue+")");
            baseDict.put(keyName, keyValue);
        }
        PropertyListParser.saveAsXML(baseDict, plistFile);
        LoggerUtil.info("Updated file "+CommonUtil.getMD5InfoString(plistFile));
    }

    public static void validatePlist(File plistFile) throws IOException {
        CommandLine commandLine = new CommandLine("plutil");
        commandLine.addArguments(new String[]{"-lint", "-s", plistFile.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

    public static void convertPlistToBinaryFormat(File plistFile) throws IOException {
        CommandLine commandLine = new CommandLine("plutil");
        commandLine.addArguments(new String[]{"-convert", "binary1", plistFile.getAbsolutePath()}, false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

}
