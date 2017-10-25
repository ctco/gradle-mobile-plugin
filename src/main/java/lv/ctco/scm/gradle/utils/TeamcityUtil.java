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

    public static void setAgentParameter(String parameter, String value) {
        logger.lifecycle("##teamcity[setParameter name='"+parameter+"' value='"+value+"']");
    }

    public static void setBuildNumber(String buildNumber) {
        logger.lifecycle("##teamcity[buildNumber '"+buildNumber+"']");
    }

    public static void setProjectReleaseVersion(String releaseVersion) {
        logger.lifecycle("##teamcity[setParameter name='project.version.iteration' value='"+releaseVersion+"']");
    }

    public static void setProjectXdepsReleaseVersion(String releaseVersion) {
        logger.lifecycle("##teamcity[setParameter name='project.xdeps.version.iteration' value='"+releaseVersion+"']");
    }

    public static void setProjectXdepsBuildVersion(String buildVersion) {
        logger.lifecycle("##teamcity[setParameter name='project.xdeps.version.publish' value='"+buildVersion+"']");
    }

    public static void setBuildStatus(String statusMessage) {
        logger.lifecycle("##teamcity[buildStatus text='"+statusMessage+"']");
    }

    public static void setErrorMessage(String errorMessage) {
        logger.lifecycle("##teamcity[message text='"+errorMessage+"' status='ERROR']");
    }

    public static void setErrorDescription(String errorDescription) {
        logger.lifecycle("##teamcity[buildProblem description='"+errorDescription+"']");
    }

}
