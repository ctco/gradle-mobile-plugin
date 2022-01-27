package lv.ctco.scm.gradle.xdeps;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;

public class XdepsRestoreDependenciesTaskDefaultConfigureAction implements Action<Task> {

    private static final Logger logger = Logging.getLogger(XdepsRestoreDependenciesTaskDefaultConfigureAction.class);

    @Override
    public void execute(Task task) {
        logger.debug("Action is configuring task {}", task.getName());
        if (task instanceof XdepsRestoreDependenciesTask) {
            XdepsRestoreDependenciesTask xdepsRestoreDependenciesTask = (XdepsRestoreDependenciesTask) task;
            xdepsRestoreDependenciesTask.setXdepsFiles(XdepsUtil.getXdepsDependencyFiles(xdepsRestoreDependenciesTask.getProject()));
            xdepsRestoreDependenciesTask.setOutputDirectory(new File(xdepsRestoreDependenciesTask.getProject().getBuildDir(), XdepsPlugin.XDEPS_OUTPUT_DIRECTORY_NAME));
        } else {
            throw new GradleException(this.getClass().getSimpleName()+" can not be applied to "+task.getClass().getSimpleName());
        }
        logger.debug("Action has configured task {}", task.getName());
    }

}
