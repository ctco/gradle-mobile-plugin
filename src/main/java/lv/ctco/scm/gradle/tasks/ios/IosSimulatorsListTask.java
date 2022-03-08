package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import java.util.List;

public class IosSimulatorsListTask extends MobilePluginTask {

    public IosSimulatorsListTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Lists all available iOS simulators");
    }

    public void doTaskAction() throws Exception {
        List<IosSimulator> iosSimulators = IosSimulatorUtil.getAvailableIosSimulators();
        getLogger().info("Found iOS simulators: {}", iosSimulators.size());
        for (IosSimulator iosSimulator : iosSimulators) {
            getLogger().info("  {}", iosSimulator);
        }
    }

}
