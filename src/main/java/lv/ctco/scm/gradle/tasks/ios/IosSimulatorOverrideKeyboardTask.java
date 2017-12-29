package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorState;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import java.io.IOException;

public class IosSimulatorOverrideKeyboardTask extends MobilePluginTask {

    public IosSimulatorOverrideKeyboardTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Overrides keyboard settings on a specific iOS simulator");
    }

    public void doTaskAction() throws Exception {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
            getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
            getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
            getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        logger.info("Checking state of {}", iosSimulator);
        if (IosSimulatorUtil.getState(iosSimulator) == IosSimulatorState.SHUTDOWN) {
            logSimulatorState(iosSimulator);
            logger.info("Overriding keyboard on {}", iosSimulator);
            IosSimulatorUtil.overrideKeyboard(iosSimulator);
        } else {
            stopWithError("iOS simulator must be shutdown to override keyboard");
        }
    }

    private void logSimulatorState(IosSimulator iosSimulator) throws IOException {
        logger.info("  state of {} is {}", iosSimulator, IosSimulatorUtil.getState(iosSimulator));
    }

}
