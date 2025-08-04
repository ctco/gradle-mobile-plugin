package lv.ctco.scm.gradle.utils;

public final class AzureDevOpsUtil {

    private AzureDevOpsUtil() {}

    public static boolean isAzureDevOpsEnvironment() {
        return PropertyUtil.hasEnvironmentProperty("TF_BUILD");
    }

    public static String generateSetParameterServiceMessage(String parameterName, String parameterValue) {
        return "##vso[task.setvariable variable="+parameterName+";isOutput=true]"+parameterValue;
    }

    public static String generateBuildNumberServiceMessage(String buildNumber) {
        return "##vso[build.updatebuildnumber]"+buildNumber;
    }

    public static String generateErrorMessageServiceMessage(String errorMessageText) {
        return "##vso[task.logissue type=error]"+errorMessageText;
    }

}
