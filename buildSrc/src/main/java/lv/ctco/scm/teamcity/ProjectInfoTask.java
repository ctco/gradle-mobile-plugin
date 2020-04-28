package lv.ctco.scm.teamcity;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

public class ProjectInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ProjectInfoTask.class);

    public ProjectInfoTask() {
        this.setGroup("TeamCity");
    }

    @TaskAction
    public void executeTaskAction() {
        logger.lifecycle("Project version is '{}'", getProject().getVersion());
        if (System.getenv("TEAMCITY_VERSION") != null) {
            logger.lifecycle("##teamcity[buildNumber '{}']", getProject().getVersion());
        }
    }

}
