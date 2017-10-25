package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecOutputStream;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.FullOutputFilter;
import lv.ctco.scm.utils.exec.NullOutputFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IosSimulatorUtil {

    private static final String EXEC_SIMCTL = "fbsimctl";

    private static final Pattern fbsimctlSimulatorPattern = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)\\|(.*)\\|(.*)");

    private IosSimulatorUtil() {}

    public static ExecResult boot(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("boot");
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static ExecResult boot(IosSimulator iosSimulator, String scale) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("boot");
        command.addArgument("--scale="+scale);
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static ExecResult erase(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("erase");
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static ExecResult shutdown(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("shutdown");
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static ExecResult overrideKeyboard(IosSimulator iosSimulator) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("keyboard_override");
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    public static ExecResult installApp(IosSimulator iosSimulator, File app) {
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument(iosSimulator.getUdid());
        command.addArgument("install");
        command.addArgument(app.getAbsolutePath(), false);
        command.addArgument("--codesign");
        return ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
    }

    private static List<String> getIosSimulatorData() {
        List<String> iosSimulatorData = new ArrayList<>();
        ExecCommand command = new ExecCommand(EXEC_SIMCTL);
        command.addArgument("list");
        ExecResult result = ExecUtil.executeCommand(command, new ExecOutputStream(new FullOutputFilter(), new NullOutputFilter()));
        if (result.isSuccess()) {
            for (String line : result.getOutput()) {
                Matcher matcher = fbsimctlSimulatorPattern.matcher(line);
                if (matcher.matches() && matcher.group(5).trim().startsWith("iOS")
                        && matcher.group(1).trim().matches("[0-9A-F]{8}-[0-9A-F]{4}-[1-5][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}")
                        ) {
                    iosSimulatorData.add(line);
                }
            }
        }
        return iosSimulatorData;
    }

    public static String getState(IosSimulator iosSimulator) throws IOException {
        for (String line : getIosSimulatorData()) {
            Matcher matcher = fbsimctlSimulatorPattern.matcher(line);
            if (matcher.matches() && matcher.group(1).trim().equals(iosSimulator.getUdid())) {
                return matcher.group(3).trim();
            }
        }
        throw new IOException("Failed to find "+iosSimulator+" to get its state");
    }

    public static List<IosSimulator> getAvailableIosSimulators() {
        List<IosSimulator> iosSimulators = new ArrayList<>();
        for (String line : getIosSimulatorData()) {
            Matcher matcher = fbsimctlSimulatorPattern.matcher(line);
            if (matcher.matches()) {
                iosSimulators.add(new IosSimulator(
                        matcher.group(1).trim(),
                        matcher.group(2).trim(),
                        matcher.group(5).trim()));
            }
        }
        return iosSimulators;
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
