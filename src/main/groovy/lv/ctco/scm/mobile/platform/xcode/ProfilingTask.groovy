/*
 * @(#)ProfilingTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import lv.ctco.scm.mobile.core.objects.Profile
import lv.ctco.scm.mobile.core.utils.ErrorUtil
import lv.ctco.scm.mobile.core.utils.GroovyProfilingUtil
import lv.ctco.scm.mobile.core.utils.PlistUtil
import lv.ctco.scm.mobile.core.utils.ProfilingUtil

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

public class ProfilingTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ProfilingTask.class)

    public File projectDir
    public List<Profile> profiles

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles
    }

    @TaskAction
    public void doTaskAction() {
        logProfiles()

        for (Profile profile : profiles) {
            File source = new File(profile.getSource())
            checkWhetherFileExists(source)
            String profileSourceName = source.getName().toLowerCase()

            if (profileSourceName.endsWith(".groovy")) {

                GroovyProfilingUtil.profileUsingGroovyEval(source)

            } else if (profileSourceName.endsWith(".plist")) {

                File target = new File(projectDir, profile.getTarget())
                checkWhetherFileExists(target)
                logger.info("Profiling file " + source.getAbsolutePath() + " to " + target.getAbsolutePath())
                PlistUtil.validatePlist(source)
                if ("Settings.bundle/Root.plist".equals(profile.getTarget()) && profile.getLevel() == 2) {
                    ProfilingUtil.profilePreferenceSpecifiersPlistEntries(target, source)
                } else {
                    ProfilingUtil.profileFirstLevelPlistEntries(target, source)
                }
                PlistUtil.validatePlist(target)

            } else {
                stopWithException("Unknown profiling source configuration - '"+profileSourceName+"'")
            }

        }
    }

    private void stopWithException(String message) {
        ErrorUtil.errorInTask(this.getName(), message)
    }

    private void checkWhetherFileExists(File file) {
        if (!file.exists()) {
            throw stopWithException("Referenced file '"+file.toString()+"' " + " does not exist")
        }
    }

    private void logProfiles() {
        if (!profiles.isEmpty()) {
            logger.info("Profiles filtered for target environment: "+profiles.size())
            for (Profile profile : profiles) {
                logger.info("  "+profile.toString())
            }
        }
    }

}
