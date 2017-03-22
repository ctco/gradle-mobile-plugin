/*
 * @(#)TeamcityUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class TeamcityUtil {

    private static final Logger logger = Logging.getLogger(TeamcityUtil.class);

    private TeamcityUtil() {}

    public static boolean isTeamcityEnvironment() {
        return PropertyUtil.hasEnvironmentProperty("TEAMCITY_VERSION");
    }

    public static void setAgentParameter(String parameter, String value) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[setParameter name='"+parameter+"' value='"+value+"']");
        }
    }

    public static void setProjectParameter(String parameter, String value) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[setProjectParameter "+parameter+"='"+value+"']");
            setAgentParameter(parameter, value);
        }
    }

    public static void setBuildNumber(String buildNumber) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[buildNumber '"+buildNumber+"']");
        }
    }

    public static void setBuildStatus(String statusMessage) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[buildStatus text='"+statusMessage+"']");
        }
    }

    public static void setErrorMessage(String errorMessage) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[message text='"+errorMessage+"' status='ERROR']");
        }
    }

    public static void setErrorDescription(String errorDescription) {
        if (isTeamcityEnvironment()) {
            logger.lifecycle("##teamcity[buildProblem description='"+errorDescription+"']");
        }
    }

}
