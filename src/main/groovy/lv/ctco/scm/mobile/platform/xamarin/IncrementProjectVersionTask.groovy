/*
 * @(#)IncrementVersionTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.utils.BackupUtil
import lv.ctco.scm.mobile.core.utils.CommonUtil
import lv.ctco.scm.mobile.core.utils.ErrorUtil
import lv.ctco.scm.mobile.core.utils.StampUtil

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

public class IncrementProjectVersionTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(IncrementProjectVersionTask.class)

    public File solutionFile
    public Csproj csproj

    @TaskAction
    public void doTaskAction() {
        try {
            String currentVersion = null
            String newVersion = null

            logger.info("Parsing solution file '" + solutionFile.getAbsolutePath() + "'")
            Solution slnObj = new SolutionParser(solutionFile).parse()

            for (SlnGlobalSection slnGlobalSection : slnObj.globalSections) {
                if (slnGlobalSection.getName().equals("MonoDevelopProperties")) {
                    currentVersion = slnGlobalSection.getProperty("version").toString()
                    logger.info("Found version '" + currentVersion + "' in solution file")
                    newVersion = StampUtil.generateNewStampVersion(currentVersion)
                    logger.info("Incrementing to '" + newVersion + "'")
                }
            }

            if (currentVersion == null) {
                throw new IOException("Version was not found in solution file")
            } else {
                Pattern patternSln = Pattern.compile("version = " + currentVersion)
                Pattern patternCsp = Pattern.compile("<ReleaseVersion>" + currentVersion + "</ReleaseVersion>")
                CommonUtil.replaceInFile(solutionFile, patternSln, "version = " + newVersion)
                for (SlnProjectSection projectSection : slnObj.projectSections) {
                    File csprojFile = new File(solutionFile.getParentFile(), projectSection.getBuildFilePath())
                    String VERSION_SECTION = csproj.getReleaseVersion()
                    if (VERSION_SECTION.equals(currentVersion)) {
                        logger.info("Updating matching version '" + VERSION_SECTION + "' in solution's section '" + projectSection.getBuildFilePath() + "'")
                        CommonUtil.replaceInFile(csprojFile, patternCsp, "<ReleaseVersion>" + newVersion + "</ReleaseVersion>")
                    } else {
                        logger.warn("Version '" + VERSION_SECTION + "' in solution's section '" + projectSection.getBuildFilePath()
                                + "' does not match version in solution. Skipping update.")
                    }
                }
                BackupUtil.applyChanges()
                logger.info("Incremented version '" + newVersion + "' has been set where applicable.")
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e)
        }
    }

}
