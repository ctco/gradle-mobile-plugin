package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.mobile.ios.IosSimulator;
import lv.ctco.scm.mobile.ios.IosSimulatorCLP;
import lv.ctco.scm.mobile.ios.IosSimulatorUtil;
import lv.ctco.scm.utils.exec.ExecResult;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class IosSimulatorBootTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(this.getClass());

    public IosSimulatorBootTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Boots a specific iOS simulator");
    }

    @TaskAction
    public void doTaskAction() throws IOException {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
                getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
                getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
                getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        logger.info("Commanding {} to boot", iosSimulator);
        logger.info("  state of {} is {}", iosSimulator, IosSimulatorUtil.getState(iosSimulator));
        ExecResult execResult;
        if (getProject().getProperties().get(IosSimulatorCLP.SCALE.getName()) != null) {
            execResult = IosSimulatorUtil.boot(iosSimulator,
                    getProject().getProperties().get(IosSimulatorCLP.SCALE.getName()).toString());
        } else {
            execResult = IosSimulatorUtil.boot(iosSimulator);
        }
        logger.info("  state of {} is {}", iosSimulator, IosSimulatorUtil.getState(iosSimulator));
        if (execResult.isSuccess()) {
            if (System.getenv("TEAMCITY_VERSION") != null) {
                logger.lifecycle("##teamcity[setParameter name='env.UITEST_SIMULATOR_ID' value='{}']", iosSimulator.getUdid());
            }
        } else {
            throw new IOException("Failed to boot iOS simulator");
        }
    }

}
