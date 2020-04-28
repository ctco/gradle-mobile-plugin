/*
 * @(#)GroovyProfilingEval.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils

import org.apache.commons.io.FileUtils

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.regex.Pattern

class GroovyProfilingEval {

    private static final Logger logger = Logging.getLogger(GroovyProfilingEval.class)

    def copyFile(String sourceFilePath, String targetFilePath) {
        File targetFile = new File(targetFilePath)
        File sourceFile = new File(sourceFilePath)
        logger.info("Replacing requested file '{}'", targetFile.getAbsolutePath())
        BackupUtil.backupFile(targetFile)
        FileUtils.copyFile(sourceFile, targetFile)
        logger.debug("  current md5={}", CommonUtil.getMD5Hex(targetFile))
    }

    def replaceInFile(String targetFilePath, String toFind, String replaceWith) {
        File targetFile = new File(targetFilePath)
        Pattern patternToFind = Pattern.compile(toFind)
        BackupUtil.backupFile(targetFile)
        CommonUtil.replaceInFile(targetFile, patternToFind, replaceWith)
    }

}
