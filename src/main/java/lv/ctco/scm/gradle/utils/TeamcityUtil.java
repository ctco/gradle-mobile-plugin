/*
 * @(#)TeamcityUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.utils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class TeamcityUtil {

    private static final Logger logger = Logging.getLogger(TeamcityUtil.class);

    private TeamcityUtil() {}

    public static boolean isTeamcityEnvironment() {
        return PropertyUtil.hasEnvironmentProperty("TEAMCITY_VERSION");
    }

    public static String generateSetParameterServiceMessage(String parameterName, String parameterValue) {
        return "##teamcity[setParameter name='"+parameterName+"' value='"+parameterValue+"']";
    }

    @Deprecated
    public static void setAgentParameter(String parameterName, String parameterValue) {
        logger.lifecycle(generateSetParameterServiceMessage(parameterName, parameterValue));
    }

    public static String generateBuildNumberServiceMessage(String buildNumber) {
        return "##teamcity[buildNumber '"+buildNumber+"']";
    }

    @Deprecated
    public static void setBuildNumber(String buildNumber) {
        logger.lifecycle(generateBuildNumberServiceMessage(buildNumber));
    }

    @Deprecated
    public static void setProjectReleaseVersion(String version) {
        logger.lifecycle(generateSetParameterServiceMessage("project.version.iteration", version));
    }

    @Deprecated
    public static void setProjectXdepsReleaseVersion(String version) {
        logger.lifecycle(generateSetParameterServiceMessage("project.xdeps.version.iteration", version));
    }

    @Deprecated
    public static void setProjectXdepsBuildVersion(String version) {
        logger.lifecycle(generateSetParameterServiceMessage("project.xdeps.version.publish", version));
    }

    public static String generateBuildStatusServiceMessage(String buildStatusText) {
        return "##teamcity[buildStatus text='"+buildStatusText+"']";
    }

    @Deprecated
    public static void setBuildStatus(String buildStatusText) {
        logger.lifecycle(generateBuildStatusServiceMessage(buildStatusText));
    }

    public static String generateErrorMessageServiceMessage(String errorMessageText) {
        return "##teamcity[message text='"+errorMessageText+"' status='ERROR']";
    }

    @Deprecated
    public static void setErrorMessage(String errorMessage) {
        logger.lifecycle(generateErrorMessageServiceMessage(errorMessage));
    }

    public static String generateBuildProblemDescriptionServiceMessage(String buildProblemDescription) {
        return "##teamcity[buildProblem description='"+buildProblemDescription+"']";
    }

    @Deprecated
    public static void setErrorDescription(String errorDescription) {
        logger.lifecycle(generateBuildProblemDescriptionServiceMessage(errorDescription));
    }

}
