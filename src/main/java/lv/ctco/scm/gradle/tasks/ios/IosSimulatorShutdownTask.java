package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorState;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import java.io.IOException;

public class IosSimulatorShutdownTask extends MobilePluginTask {

    public IosSimulatorShutdownTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Shuts down a specific iOS simulator");
    }

    public void doTaskAction() throws Exception {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
            getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
            getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
            getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        getLogger().info("Checking state of {}", iosSimulator);
        logSimulatorState(iosSimulator);
        if (IosSimulatorUtil.getState(iosSimulator) != IosSimulatorState.SHUTDOWN) {
            getLogger().info("Commanding {} to shutdown", iosSimulator);
            if (IosSimulatorUtil.shutdown(iosSimulator).isFailure()) {
                stopWithError("Failed to shut down iOS simulator");
            }
            logSimulatorState(iosSimulator);
        }
    }

    private void logSimulatorState(IosSimulator iosSimulator) throws IOException {
        getLogger().info("  state of {} is {}", iosSimulator, IosSimulatorUtil.getState(iosSimulator));
    }

}
