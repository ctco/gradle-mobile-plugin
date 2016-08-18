/*
 * @(#)UIAutomationSetup.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects

class UIAutomationSetup {

    String buildTarget
    String applyProfile
    String appName
    String appPath
    String resultsPath
    String targetDevice
    String[] jsPath

    UIAutomationSetup() {}

    UIAutomationSetup(String buildTarget, String applyProfile, String appName, File appPath,
                      File resultsPath, String targetDevice, String[] jsPath) {
        this.buildTarget = buildTarget
        this.applyProfile = applyProfile
        this.appName = appName
        this.appPath = appPath
        this.jsPath = jsPath
        this.targetDevice = targetDevice
        this.resultsPath = resultsPath
    }

    @Override
    public String toString() {
        String jsPaths
        for (String path : jsPath) {
            if (jsPaths == null) {
                jsPaths = path
            } else {
                jsPaths = jsPaths + "\n            " + path
            }
        }
        return "UI Automation Setup{" +
                (buildTarget == null ? "" : "\n    buildTarget='" + buildTarget + '\'') +
                (applyProfile == null ? "" : "\n    applyProfile='" + applyProfile + '\'') +
                (appName == null ? "" : "\n    appName='" + appName + '\'') +
                (appPath == null ? "" : "\n    appPath='" + appPath + '\'') +
                (resultsPath == null ? "" : "\n    configuration='" + resultsPath + '\'') +
                (targetDevice == null ? "" : "\n    targetDevice='" + targetDevice + '\'') +
                (jsPath == null ? "" : "\n    jsPath=[" + jsPaths + ']') +
                '\n  }'
    }

}
