/*
 * @(#)ErrorUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.utils;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class ErrorUtil {

    private static final Logger logger = Logging.getLogger(ErrorUtil.class);

    private ErrorUtil() {}

    private static void printError(String taskName, String errorMessage) {
        logger.error(errorMessage);
        if (TeamcityUtil.isTeamcityEnvironment()) {
            logger.lifecycle(TeamcityUtil.generateBuildStatusServiceMessage("Execution failed for task "+taskName));
            logger.lifecycle(TeamcityUtil.generateBuildProblemDescriptionServiceMessage(errorMessage));
        }
        if (AzureDevOpsUtil.isAzureDevOpsEnvironment()) {
            logger.lifecycle(AzureDevOpsUtil.generateErrorMessageServiceMessage(errorMessage));
        }
    }

    public static void errorInTask(String taskName, String errorMessage) {
        printError(taskName, errorMessage);
        throw new GradleException(errorMessage);
    }

    public static void errorInTask(String taskName, Exception exception) {
        printError(taskName, exception.getMessage());
        throw new GradleException(exception.getMessage(), exception);
    }

}
