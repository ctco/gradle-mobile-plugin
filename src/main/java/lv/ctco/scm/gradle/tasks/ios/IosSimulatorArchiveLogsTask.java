package lv.ctco.scm.gradle.tasks.ios;

import lv.ctco.scm.gradle.MobilePluginTask;
import lv.ctco.scm.mobile.utils.ZipUtil;
import lv.ctco.scm.mobile.utils.IosSimulator;
import lv.ctco.scm.mobile.utils.IosSimulatorCLP;
import lv.ctco.scm.mobile.utils.IosSimulatorUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class IosSimulatorArchiveLogsTask extends MobilePluginTask {

    public IosSimulatorArchiveLogsTask() {
        this.setGroup("iOS Simulator");
        this.setDescription("Archives logs of a specific iOS simulator");
    }

    public void doTaskAction() throws Exception {
        IosSimulator iosSimulator = IosSimulatorUtil.findSimulator(
                getProject().getProperties().get(IosSimulatorCLP.UDID.getName()),
                getProject().getProperties().get(IosSimulatorCLP.TYPE.getName()),
                getProject().getProperties().get(IosSimulatorCLP.RUNTIME.getName())
        );
        File simLogDir = new File(FileUtils.getUserDirectory(), "Library/Logs/CoreSimulator/"+iosSimulator.getUdid());
        Collection<File> simLogs = FileUtils.listFiles(simLogDir, new String[] {"log", "gz"}, false);
        if (simLogs.isEmpty()) {
            getLogger().info("Simulator logs were not found");
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
            getLogger().info("Simulator logs were archived to '{}'", logZip.getAbsolutePath());
        }
    }

}
