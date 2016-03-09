/*
 * @(#)ProfilingTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Profile
import lv.ctco.scm.mobile.core.utils.BackupUtil
import lv.ctco.scm.mobile.core.utils.CommonUtil
import lv.ctco.scm.mobile.core.utils.GroovyProfilingUtil
import lv.ctco.scm.mobile.core.utils.LoggerUtil
import lv.ctco.scm.mobile.core.utils.PlistUtil
import lv.ctco.scm.mobile.core.utils.ProfilingUtil

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ProfilingTask extends DefaultTask {

    /** Name of the project to build. */
    String projectName = ""

    /** Name of the environment to profile for. */
    String environmentName = ""

    boolean enforcePlistSyntax = false

    /** Profile targets and sources for environments. */
    Profile[] profiles

    @TaskAction
    public void doTaskAction() {
        boolean isInfoProfiled = false
        boolean isRootProfiled = false
        File projectDir = new File(projectName)
        for (Profile profile : profiles) {
            if (profile.getEnvironment().equals(environmentName) && profile.hasScope('build')) {
                for (String pathSource : profile.getSources().split(",")) {
                    File base = null
                    if (!pathSource.toLowerCase().endsWith('.groovy')) {
                        base = new File(projectDir, profile.getTarget())
                        if (!base.exists()) {
                            throw new GradleException(base.toString() + " does not exist")
                        }
                    }

                    File prof = new File(projectDir, pathSource)
                    if (!prof.exists()) {
                        throw new GradleException(prof.toString() + " does not exist")
                    }

                    if (base != null) {
                        if (base.getName().equals("Info.plist")) {
                            isInfoProfiled = true
                        }
                        if (base.getName().equals("Root.plist")) {
                            isRootProfiled = true
                        }
                        LoggerUtil.info("Profiling file " + prof.canonicalPath + " to " + base.canonicalPath)
                    }

                    def profileSourceType = prof.getName().toLowerCase()
                    if (profileSourceType.endsWith(".groovy")) {
                        GroovyProfilingUtil.profileUsingGroovyEval(prof)
                    } else if (profileSourceType.endsWith(".tt")) {
                        for (String pathAdditional : profile.getAdditionalTargets()) {
                            BackupUtil.backupFile(new File(projectDir, pathAdditional))
                        }
                        ProfilingUtil.profileUsingT4Templates(base, prof, environmentName)
                    } else {
                        PlistUtil.validatePlist(prof)
                        ProfilingUtil.profileUsingPlistEntries(base, prof)
                    }

                    if (base != null) {
                        if (base.getName().toLowerCase().endsWith('.plist') && enforcePlistSyntax) {
                            PlistUtil.validatePlist(base)
                        }
                    }
                }
            }
        }

        if (!isInfoProfiled) {
            File infoPlist = new File(projectDir, "Info.plist")
            if (infoPlist.exists()) {
                LoggerUtil.info("Imitating Info.plist update as there is no profiling configuration...")
                CommonUtil.addNewlineAtEndOfFile(infoPlist)
                if (enforcePlistSyntax) {
                    PlistUtil.validatePlist(infoPlist)
                }
            } else {
                LoggerUtil.info("No Info.plist in default path...")
            }
        }
        if (!isRootProfiled) {
            File rootPlist = new File(projectDir, "Settings.bundle/Root.plist")
            if (rootPlist.exists()) {
                LoggerUtil.info("Imitating Root.plist update as there is no profiling configuration...")
                CommonUtil.addNewlineAtEndOfFile(rootPlist)
                if (enforcePlistSyntax) {
                    PlistUtil.validatePlist(rootPlist)
                }
            } else {
                LoggerUtil.info("No Root.plist in default path...")
            }
        }

    }

}
