package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.mobile.ios.IosSimulator;
import lv.ctco.scm.mobile.ios.IosSimulatorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

public class IosSimulatorsListTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(IosSimulatorsListTask.class);

    public IosSimulatorsListTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Lists all available iOS simulators");
    }

    @TaskAction
    public void doTaskAction() {
        List<IosSimulator> iosSimulators = IosSimulatorUtil.getAvailableIosSimulators();
        logger.info("Found iOS simulators: {}", iosSimulators.size());
        for (IosSimulator iosSimulator : iosSimulators) {
            logger.info("  {}", iosSimulator);
        }
    }

}
