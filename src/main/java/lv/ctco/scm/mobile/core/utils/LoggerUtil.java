/*
 * @(#)LoggerUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class LoggerUtil {

    private static final Logger LOGGER = Logging.getLogger("MobilePlugin");

    private LoggerUtil() {}

    public static void errorInTask(String taskName, String errorDescription) {
        LOGGER.error(errorDescription);
        if (TeamcityUtil.isTeamcityEnvironment()) {
            TeamcityUtil.setBuildStatus("Execution failed for task "+taskName);
            TeamcityUtil.setErrorDescription(errorDescription);
        }
    }

    /**
     * Used for logging error messages.
     * @param message Message to log.
     */
    public static void error(String message) {
        LOGGER.error(message);
    }

    /**
     * Used for logging important information messages.
     * @param message Message to log.
     */
    public static void quiet(String message) {
        LOGGER.quiet(message);
    }

    /**
     * Used for logging warning messages.
     * @param message Message to log.
     */
    public static void warn(String message) {
        LOGGER.warn(message);
    }

    /**
     * Used for logging progress information messages.
     * @param message Message to log.
     */
    public static void lifecycle(String message) {
        LOGGER.lifecycle(message);
    }

    /**
     * Used for logging information messages.
     * @param message Message to log.
     */
    public static void info(String message) {
        LOGGER.info(message);
    }

    /**
     * Used for logging debug messages.
     * @param message Message to log.
     */
    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

}
