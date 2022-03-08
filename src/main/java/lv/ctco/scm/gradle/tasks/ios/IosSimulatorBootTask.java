package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorState;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import java.io.IOException;

public class IosSimulatorBootTask extends MobilePluginTask {

    public IosSimulatorBootTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Boots a specific iOS simulator");
    }

    public void doTaskAction() throws Exception {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
            getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
            getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
            getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        getLogger().info("Checking state of {}", iosSimulator);
        logSimulatorState(iosSimulator);
        if (IosSimulatorUtil.getState(iosSimulator) == IosSimulatorState.SHUTDOWN) {
            getLogger().info("Commanding {} to boot", iosSimulator);
            if (IosSimulatorUtil.boot(iosSimulator).isSuccess()) {
                logSimulatorState(iosSimulator);
                if (System.getenv("TEAMCITY_VERSION") != null) {
                    getLogger().lifecycle("##teamcity[setParameter name='env.UITEST_SIMULATOR_ID' value='{}']", iosSimulator.getUdid());
                    getLogger().lifecycle("##teamcity[setParameter name='env.UITEST_SIMULATOR_NAME' value='{}']", iosSimulator.getName());
                    getLogger().lifecycle("##teamcity[setParameter name='env.UITEST_SIMULATOR_SDK_VERSION' value='{}']", iosSimulator.getSdkVersion());
                }
            } else {
                logSimulatorState(iosSimulator);
                stopWithError("Failed to boot iOS simulator");
            }
        }
    }

    private void logSimulatorState(IosSimulator iosSimulator) throws IOException {
        getLogger().info("  state of {} is {}", iosSimulator, IosSimulatorUtil.getState(iosSimulator));
    }

}
