/*
 * @(#)StampUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public final class StampUtil {

    private static final Integer EXT_DEPLOYMENT_SUFFIX_POSITION = 1;
    private static final String EXT_DEPLOYMENT_SUFFIX_NEW = ".0";

    private StampUtil() {}

    public static String generateNewStampVersion(String stamp) throws IOException {
        String tmpStamp = sanitiseStamp(stamp);
        String[] digits = splitStamp(tmpStamp);
        Integer extDepNum = digits.length-EXT_DEPLOYMENT_SUFFIX_POSITION;
        String externalDeploymentOfStamp = getExternalDeploymentOfStamp(tmpStamp);
        Integer deployDigit = Integer.parseInt(externalDeploymentOfStamp);
        deployDigit = deployDigit+1;
        digits[extDepNum] = deployDigit.toString();
        return transformStampToString(digits);
    }

    static String updateStamp(String oldStamp, String newIterationVersion) {
        String oldIterationVersion = getIterationVersionFromStamp(oldStamp);
        String stamp;
        if(oldIterationVersion.equals(newIterationVersion)) {
            LoggerUtil.info("Iteration version are equal");
            stamp = oldIterationVersion;
        } else {
            LoggerUtil.info("Setting new iteration version");
            stamp = iterationToStamp(newIterationVersion);
            if (TeamcityUtil.isTeamcityEnvironment()) {
                TeamcityUtil.setProjectParameter("stamp", stamp);
            }
        }
        return stamp;
    }

    protected static String getExternalDeploymentOfStamp(String stamp) throws IOException {
        String number;
        String tmpStamp = sanitiseStamp(stamp);
        if (StringUtils.isNotBlank(tmpStamp) && tmpStamp.contains(".")) {
            String[] parts = splitStamp(tmpStamp);
            if (parts.length > EXT_DEPLOYMENT_SUFFIX_POSITION) {
                number = parts[parts.length-EXT_DEPLOYMENT_SUFFIX_POSITION];
                LoggerUtil.debug("external deployment: "+number);
                return number;
            }
        }
        throw new IOException("Bad stamp for external deployment retrieving: "+tmpStamp);
    }

    protected static String iterationToStamp(String iterationVersion) {
        return sanitiseStamp(iterationVersion)+EXT_DEPLOYMENT_SUFFIX_NEW;
    }

    protected static String getIterationVersionFromStamp(String stamp) {
        String[] splitStamp = splitStamp(sanitiseStamp(stamp));
        int iterationVersionSize = splitStamp.length-EXT_DEPLOYMENT_SUFFIX_POSITION;
        String[] iterationVersionArray = new String[iterationVersionSize];
        System.arraycopy(splitStamp, 0, iterationVersionArray, 0, iterationVersionSize);
        return transformStampToString(iterationVersionArray);
    }

    private static String[] splitStamp(String stamp) {
        return stamp.split("\\.");
    }

    protected static String sanitiseStamp(String stamp) {
        String result = stamp.trim();
        result = result.replaceAll("(^\\.*)", "");
        result = result.replaceAll("(\\.*$)", "");
        return result.trim();
    }

    private static String transformStampToString(String[] arrStamp) {
        StringBuilder newStamp = new StringBuilder();
        for (int i = 0; i < arrStamp.length; ++i) {
            newStamp.append(arrStamp[i]);
            if (i < arrStamp.length-1) {
                newStamp.append('.');
            }
        }
        LoggerUtil.debug("Transforming array to string: "+newStamp.toString());
        return newStamp.toString();
    }

}
