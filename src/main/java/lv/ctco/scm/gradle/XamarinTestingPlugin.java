package lv.ctco.scm.gradle;

import lv.ctco.scm.gradle.tasks.ios.IosSimulatorArchiveLogsTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorBootTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorEraseTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorInstallAppTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorOverrideKeyboardTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorShutdownTask;
import lv.ctco.scm.gradle.tasks.ios.IosSimulatorsListTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class XamarinTestingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("listIosSimulators", IosSimulatorsListTask.class);
        project.getTasks().create("bootIosSimulator", IosSimulatorBootTask.class);
        project.getTasks().create("overrideKeyboardOnIosSimulator", IosSimulatorOverrideKeyboardTask.class);
        project.getTasks().create("installAppOnIosSimulator", IosSimulatorInstallAppTask.class);
        project.getTasks().create("archiveLogsOfIosSimulator", IosSimulatorArchiveLogsTask.class);
        project.getTasks().create("eraseIosSimulator", IosSimulatorEraseTask.class);
        project.getTasks().create("shutdownIosSimulator", IosSimulatorShutdownTask.class);
    }

}
