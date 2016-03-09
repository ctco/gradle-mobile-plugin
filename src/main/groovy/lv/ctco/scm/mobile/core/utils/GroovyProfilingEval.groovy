/*
 * @(#)GroovyProfilingEval.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import org.apache.commons.io.FileUtils

import java.util.regex.Pattern

class GroovyProfilingEval {

    def copyFile(String sourceFilePath, String targetFilePath) {
        File targetFile = new File(targetFilePath)
        File sourceFile = new File(sourceFilePath)
        BackupUtil.backupFile(targetFile)
        FileUtils.copyFile(sourceFile, targetFile)
        LoggerUtil.info("Updated file "+CommonUtil.getMD5InfoString(targetFile))
    }

    def replaceInFile(String targetFilePath, String toFind, String replaceWith) {
        File targetFile = new File(targetFilePath)
        Pattern patternToFind = Pattern.compile(toFind)
        BackupUtil.backupFile(targetFile)
        CommonUtil.replaceInFile(targetFile, patternToFind, replaceWith)
    }

}
