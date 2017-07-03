package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.mobile.core.utils.ZipUtil;
import lv.ctco.scm.mobile.ios.IosSimulator;
import lv.ctco.scm.mobile.ios.IosSimulatorCLP;
import lv.ctco.scm.mobile.ios.IosSimulatorUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class IosSimulatorArchiveLogsTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(this.getClass());

    public IosSimulatorArchiveLogsTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Archives logs of a specific iOS simulator");
    }

    @TaskAction
    public void doTaskAction() throws IOException {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
                getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
                getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
                getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        File simLogDir = new File(FileUtils.getUserDirectory(), "Library/Logs/CoreSimulator/"+iosSimulator.getUdid());
        Collection<File> simLogs = FileUtils.listFiles(simLogDir, new String[] {"log", "gz"}, false);
        if (simLogs.isEmpty()) {
            logger.info("Simulator logs were not found");
        } else {
            File tmpLogDir = new File("build/gmp-temp/simulator-logs/");
            FileUtils.forceMkdir(tmpLogDir);
            FileUtils.cleanDirectory(tmpLogDir);
            for (File simLog : simLogs) {
                FileUtils.copyFileToDirectory(simLog, tmpLogDir);
            }
            File logDir = new File("build/logs/");
            FileUtils.forceMkdir(logDir);
            File logZip = new File(logDir, "simulator-logs.zip");
            ZipUtil.compressDirectory(tmpLogDir, false, logZip);
            FileUtils.deleteDirectory(tmpLogDir);
            logger.info("Simulator logs were archived to '{}'", logZip.getAbsolutePath());
        }
    }

}
