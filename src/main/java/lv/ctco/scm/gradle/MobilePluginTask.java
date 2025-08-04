package lv.ctco.scm.gradle;

import lv.ctco.scm.gradle.utils.AzureDevOpsUtil;
import lv.ctco.scm.gradle.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

public abstract class MobilePluginTask extends DefaultTask {

    @Deprecated
    protected final Logger logger = Logging.getLogger(this.getClass());

    @TaskAction
    private void main() {
        try {
            doTaskAction();
        } catch (Exception e) {
            stopWithError(e);
        }
    }

    public abstract void doTaskAction() throws Exception;

    protected void stopWithError(String errorMessage) {
        throw new GradleException(errorMessage);
    }

    protected void stopWithError(Exception exception) {
        printError(exception.getMessage());
        throw new GradleException(exception.getMessage(), exception);
    }

    private void printError(String errorMessage) {
        getLogger().error(errorMessage);
        if (TeamcityUtil.isTeamcityEnvironment()) {
            getLogger().lifecycle(TeamcityUtil.generateBuildStatusServiceMessage("Execution failed for task "+this.getPath()));
            getLogger().lifecycle(TeamcityUtil.generateBuildProblemDescriptionServiceMessage(errorMessage));
        }
        if (AzureDevOpsUtil.isAzureDevOpsEnvironment()) {
            getLogger().lifecycle(AzureDevOpsUtil.generateErrorMessageServiceMessage(errorMessage));
        }
    }

}
