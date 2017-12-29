package lv.ctco.scm.mobile.utils;

import com.dd.plist.NSDictionary;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class IosSimulatorUtil {

    private static final String EXEC_XCRUN = "xcrun";
    private static final String EXEC_XCRUN_SIMCTL = "simctl";

    private static final File devicesDir = new File(FileUtils.getUserDirectory(), "Library/Developer/CoreSimulator/Devices");

    private IosSimulatorUtil() {}

    public static ExecResult boot(IosSimulator iosSimulator) throws InterruptedException {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("boot");
        command.addArgument(iosSimulator.getUdid());
        ExecResult boot = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (boot.isSuccess()) {
            // Additionally wait up to 30 seconds for simulator endpoints
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000);
                if (areExpectedIosSimulatorEndpointsActive(iosSimulator)) {
                    break;
                }
            }
        }
        return boot;
    }

    public static ExecResult shutdown(IosSimulator iosSimulator) throws InterruptedException {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("shutdown");
        command.addArgument(iosSimulator.getUdid());
        ExecResult shutdown = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (shutdown.isSuccess()) {
            // Additionally wait up to 30 seconds for launchd_sim process termination
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000);
                if (getIosSimulatorRootProcessID(iosSimulator) == null) {
                    break;
                }
            }
        }
        return shutdown;
    }

    public static ExecResult erase(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("erase");
        command.addArgument(iosSimulator.getUdid());
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static void overrideKeyboard(IosSimulator iosSimulator) throws IOException {
        File preferencesFile = new File(devicesDir, iosSimulator.getUdid()+"/data/Library/Preferences/com.apple.Preferences.plist");
        NSDictionary preferencesData;
        if (preferencesFile.exists()) {
            preferencesData = PlistUtil.getRootDictionary(preferencesFile);
        } else {
            preferencesData = new NSDictionary();
        }
        preferencesData.put("KeyboardAllowPaddle", false);
        preferencesData.put("KeyboardAssistant", false);
        preferencesData.put("KeyboardAutocapitalization", false);
        preferencesData.put("KeyboardAutocorrection", false);
        preferencesData.put("KeyboardCapsLock", false);
        preferencesData.put("KeyboardCheckSpelling", false);
        preferencesData.put("KeyboardPeriodShortcut", false);
        preferencesData.put("KeyboardPrediction", false);
        preferencesData.put("KeyboardShowPredictionBar", false);
        preferencesData.put("HWKeyboardAutocapitalization", false);
        preferencesData.put("HWKeyboardAutocorrection", false);
        preferencesData.put("HWKeyboardPeriodShortcut", false);
        preferencesData.put("SmartDashesEnabled", false);
        preferencesData.put("SmartQuotesEnabled", false);
        PlistUtil.saveAsBinaryPlist(preferencesData, preferencesFile);
    }

    public static ExecResult installApp(IosSimulator iosSimulator, File app) {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("install");
        command.addArgument(iosSimulator.getUdid());
        command.addArgument(app.getAbsolutePath(), false);
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    private static String getIosSimulatorRootProcessID(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand("ps");
        command.addArgument("-x").addArgument("-o").addArgument("pid,command");
        ExecResult ps = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (ps.isSuccess()) {
            for (String line : ps.getOutput()) {
                String[] processInfo = StringUtils.split(line, " ", 2);
                if (processInfo[1].contains("launchd_sim")
                        && processInfo[1].contains(iosSimulator.getUdid())) {
                    return processInfo[0];
                }
            }
        }
        return null;
    }

    private static boolean areExpectedIosSimulatorEndpointsActive(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL).addArgument("spawn").addArgument(iosSimulator.getUdid());
        command.addArgument("launchctl").addArgument("print").addArgument("system");
        ExecResult list = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (list.isSuccess()) {
            List<String> expectedEndpoints = new ArrayList<>();
            expectedEndpoints.add("A   com.apple.springboard.services");
            expectedEndpoints.add("A   com.apple.accessibility.AXSpringBoardServer");
            int activeEndpoints = 0;
            for (String line : list.getOutput()) {
                for (String endpoint : expectedEndpoints) {
                    if (line.contains(endpoint)) {
                        activeEndpoints++;
                    }
                }
            }
            return activeEndpoints == expectedEndpoints.size();
        } else {
            return false;
        }
    }

    private static String getIosSimulatorRuntimesJson() throws IOException {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("list");
        command.addArgument("runtimes");
        command.addArgument("--json");
        ExecResult result = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (result.isSuccess()) {
            return StringUtils.join(result.getOutput(), "");
        } else {
            throw new IOException("Failed to get iOS simulator runtimes");
        }
    }

    private static String getIosSimulatorDevicesJson() throws IOException {
        ExecCommand command = new ExecCommand(EXEC_XCRUN);
        command.addArgument(EXEC_XCRUN_SIMCTL);
        command.addArgument("list");
        command.addArgument("devices");
        command.addArgument("--json");
        ExecResult result = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (result.isSuccess()) {
            return StringUtils.join(result.getOutput(), "");
        } else {
            throw new IOException("Failed to get iOS simulator runtimes");
        }
    }

    static List<IosSimulatorRuntime> getAvailableIosSimulatorRuntimes(String json) {
        List<IosSimulatorRuntime> runtimes = new ArrayList<>();
        for (JsonValue item : Json.parse(json).asObject().get("runtimes").asArray()) {
            String availability = item.asObject().getString("availability", "");
            String name = item.asObject().getString("name", "");
            if (name.startsWith("iOS") && availability.equals("(available)")) {
                String identifier = item.asObject().getString("identifier", "");
                IosSimulatorRuntime rt = new IosSimulatorRuntime(identifier, name);
                runtimes.add(rt);
            }
        }
        return runtimes;
    }

    private static List<IosSimulatorRuntime> getAvailableIosSimulatorRuntimes() throws IOException {
        return getAvailableIosSimulatorRuntimes(getIosSimulatorRuntimesJson());
    }

    static List<String> getAvailableIosSimulatorIdentifiers(String json) throws IOException {
        List<IosSimulatorRuntime> runtimes = getAvailableIosSimulatorRuntimes();
        List<String> identifiers = new ArrayList<>();
        JsonValue jRoot = Json.parse(json).asObject().get("devices");
        for (IosSimulatorRuntime runtime : runtimes) {
            JsonValue jRunt = jRoot.asObject().get(runtime.getName());
            for (JsonValue jSimulator : jRunt.asArray()) {
                String availability = jSimulator.asObject().getString("availability", "");
                if (availability.equals("(available)")) {
                    String identifier = jSimulator.asObject().getString("udid", "");
                    identifiers.add(identifier);
                }
            }
        }
        return identifiers;
    }

    public static List<IosSimulator> getAvailableIosSimulators() throws IOException {
        List<IosSimulator> simulators = new ArrayList<>();
        for (String identifier : getAvailableIosSimulatorIdentifiers(getIosSimulatorDevicesJson())) {
            File devicePlistFile = new File(devicesDir, identifier+"/device.plist");
            NSDictionary devicePlist = PlistUtil.getRootDictionary(devicePlistFile);
            if (devicePlist.get("runtime").toString().contains("SimRuntime.iOS")) {
                IosSimulator simulator = new IosSimulator(
                        devicePlist.get("UDID").toString(),
                        devicePlist.get("name").toString(),
                        devicePlist.get("deviceType").toString(),
                        devicePlist.get("runtime").toString()
                );
                simulators.add(simulator);
            }
        }
        return simulators;
    }

    public static IosSimulatorState getState(IosSimulator iosSimulator) throws IOException {
        File devicePlistFile = new File(devicesDir, iosSimulator.getUdid()+"/device.plist");
        if (devicePlistFile.exists()) {
            String state = PlistUtil.getStringValue(devicePlistFile, "state");
            switch (state) {
                case "1":
                    return IosSimulatorState.SHUTDOWN;
                case "2":
                    return IosSimulatorState.BOOTING;
                case "3":
                    return IosSimulatorState.BOOTED;
                default:
                    throw new IOException("Unknown simulator state");
            }
        } else {
            throw new IOException("Failed to find simulator device plist");
        }
    }

    public static IosSimulator findSimulator(Object udid, Object type, Object runtime) throws IOException {
        if (udid != null) {
            return findSimulatorByUdid(udid.toString());
        } else if (type != null && runtime != null) {
            return findSimulatorByType(type.toString(), runtime.toString());
        } else if (type != null) {
            return findSimulatorByType(type.toString());
        } else {
            throw new IOException("Unable to find simulator with provided properties");
        }
    }

    public static IosSimulator findSimulatorByUdid(String udid) throws IOException {
        List<IosSimulator> iosSimulators = getAvailableIosSimulators();
        for (IosSimulator iosSimulator : iosSimulators) {
            if (udid.equals(iosSimulator.getUdid())) {
                return iosSimulator;
            }
        }
        throw new IOException("iOS simulator matching udid:"+udid+" not found");
    }

    public static IosSimulator findSimulatorByType(String type, String runtime) throws IOException {
        List<IosSimulator> iosSimulators = getAvailableIosSimulators();
        for (IosSimulator iosSimulator : iosSimulators) {
            if (type.equals(iosSimulator.getType()) && runtime.equals(iosSimulator.getRuntime())) {
                return iosSimulator;
            }
        }
        throw new IOException("iOS simulator matching type:"+type+" and runtime:"+runtime+" not found");
    }

    public static IosSimulator findSimulatorByType(String type) throws IOException {
        List<IosSimulator> iosSimulators = new ArrayList<>();
        for (IosSimulator iosSimulator : getAvailableIosSimulators()) {
            if (type.equals(iosSimulator.getType())) {
                iosSimulators.add(iosSimulator);
            }
        }
        if (iosSimulators.isEmpty()) {
            throw new IOException("iOS simulator matching type:"+type+" not found");
        } else if (iosSimulators.size() > 1) {
            throw new IOException("iOS simulator matching type:"+type+" is not unique");
        } else {
            return iosSimulators.get(0);
        }
    }

}
