/*
 * @(#)IosSimulatorUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Singleton
public class IosSimulatorUtil {

    private IosSimulatorUtil() {}

    public static String getKnownDevice(String targetDevice) throws IOException {
        CommandLine commandLine = new CommandLine("instruments");
        // TODO : Redo to '-s devices'
        commandLine.addArgument("-w");
        commandLine.addArgument("list");
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, false);
        String device = null;
        if (!execResult.getOutput().isEmpty()) {
            for (String line : execResult.getOutput()) {
                if (line.equals(targetDevice)) {
                    device = line;
                }
                if (line.startsWith(targetDevice + " - Simulator")) {
                    device = line;
                }
            }
        }
        return device;
    }

    public static void activateDefaultSimulator() throws IOException {
        File simulatorDir = PathUtil.getXcodeSimulatorDir();
        if (simulatorDir == null) {
            throw new IOException("iOS simulator was not found!");
        } else {
            String simulatorAppName = FilenameUtils.getBaseName(simulatorDir.getName());
            CommandLine commandLine = new CommandLine("osascript");
            commandLine.addArgument("-e");
            commandLine.addArgument("\"tell application \\\"" + simulatorAppName + "\\\" to activate\"", false);
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
            if (!execResult.isSuccess()) {
                throw new IOException(execResult.getException());
            }
            //Thread.sleep(3000);
        }
    }

    public static void quitDefaultSimulator() throws IOException {
        File simulatorDir = PathUtil.getXcodeSimulatorDir();
        if (simulatorDir == null) {
            throw new IOException("iOS simulator was not found!");
        } else {
            String simulatorAppName = FilenameUtils.getBaseName(simulatorDir.getName());
            CommandLine commandLine = new CommandLine("osascript");
            commandLine.addArgument("-e");
            commandLine.addArgument("\'tell application \""+simulatorAppName+"\" to quit\'", true);
            ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, false, false);
            if (!execResult.isSuccess()) {
                throw new IOException(execResult.getException());
            }
            //Thread.sleep(3000);
        }
    }

    public static void deleteApp(String appName) throws IOException {
        List<String> simulatorRootPaths = new ArrayList<>();
        simulatorRootPaths.add(System.getProperty("user.home") + "/Library/Application Support/iPhone Simulator");
        simulatorRootPaths.add(System.getProperty("user.home") + "/Library/Developer/CoreSimulator/Devices");
        for (String simulatorRootPath : simulatorRootPaths) {
            File simulatorRootDir = new File(simulatorRootPath);
            if (simulatorRootDir.exists()) {
                Collection files = FileUtils.listFiles(simulatorRootDir, null, true);
                LoggerUtil.info("Checking " + simulatorRootDir.toString() + " for existing " + appName + " apps");
                for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                    File file = (File)iterator.next();
                    if (file.getName().equals(appName) && file.getParentFile().getAbsolutePath().endsWith(appName+".app")) {
                        File appDir = file.getParentFile().getParentFile();
                        if (appDir.exists()) {
                            FileUtils.deleteDirectory(appDir);
                            LoggerUtil.info("Found and removed "+appName+".app at "+appDir.getAbsolutePath());
                        }
                    }
                }
            } else {
                LoggerUtil.warn("Failed to clean iPhoneSimulator as it was not found at "+simulatorRootPath);
            }
        }
    }

}
