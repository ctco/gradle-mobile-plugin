package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;
import lv.ctco.scm.utils.exec.ExecResult;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class IosSimulatorEraseTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(this.getClass());

    public IosSimulatorEraseTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Erases a specific iOS simulator");
    }

    @TaskAction
    public void doTaskAction() throws IOException {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
                getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
                getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
                getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        logger.info("Erasing {}", iosSimulator);
        ExecResult execResult = IosSimulatorUtil.erase(iosSimulator);
        if (!execResult.isSuccess()) {
            throw new IOException("Failed to erase iOS simulator");
        }
    }

}
