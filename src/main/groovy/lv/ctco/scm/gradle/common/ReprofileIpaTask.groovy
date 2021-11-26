/*
 * @(#)ReprofileIpaTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.common

import lv.ctco.scm.gradle.MobileExtension
import lv.ctco.scm.gradle.TaskGroup
import lv.ctco.scm.mobile.utils.Profile
import lv.ctco.scm.gradle.utils.PropertyUtil
import lv.ctco.scm.mobile.utils.ReprofilingUtil
import lv.ctco.scm.gradle.xamarin.XamarinConfiguration
import lv.ctco.scm.gradle.xcode.XcodeConfiguration

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ReprofileIpaTask extends DefaultTask {

    MobileExtension ctcoMobile
    List<Profile> profiles = new ArrayList<>()

    public ReprofileIpaTask() {
        this.setGroup(TaskGroup.UTILITY.getLabel())
        this.setDescription("Re-profiles and re-signs an IPA file to other environment")
    }

    @TaskAction
    public void doTaskAction() {
        checkProvidedParameters()

        String targetEnvName = PropertyUtil.getProjectProperty(getProject(), "reprofiling.environment")
        File targetIpaFile = new File(PropertyUtil.getProjectProperty(getProject(),"reprofiling.artifact"))
        if (targetEnvName == null || targetEnvName.isEmpty()) {
            throw new GradleException('Reprofiling target environment name has not been provided!')
        }
        if (!targetIpaFile.getName().toLowerCase().endsWith('.ipa')) {
            throw new GradleException('Non-IPA reprofiling target artifact has been provided!')
        }
        if (!targetIpaFile.exists()) {
            throw new GradleException('Reprofiling target artifact has not been found!')
        }

        if (getProject().hasProperty('ctcoMobile')) {
            String platform = getProject().ctcoMobile.platform
            if (platform.equals('xcode')) {
                XcodeConfiguration configuration = getProject().ctcoMobile.xcode.getXcodeConfiguration()
                profiles = configuration.getSpecificProfiles(targetEnvName, "artifact")
            } else if (platform.equals('xamarin')) {
                XamarinConfiguration configuration = getProject().ctcoMobile.xamarin.getXamarinConfiguration()
                profiles = configuration.getSpecificProfiles(targetEnvName, "artifact")
            }
        }

        boolean verify
        if (PropertyUtil.hasProjectProperty("reprofiling.verify")) {
            verify = Boolean.parseBoolean(PropertyUtil.getProjectProperty(getProject(), "reprofiling.verify"))
        } else {
            verify = true
        }
        ReprofilingUtil.reprofileIpa(getProject(), targetIpaFile, targetEnvName, profiles, verify)
    }

    private void checkProvidedParameters() {
        if (!(PropertyUtil.hasProjectProperty(getProject(), "reprofiling.artifact")
                && PropertyUtil.hasProjectProperty(getProject(), "reprofiling.environment"))) {
            throw new GradleException("Required parameters have not been provided!")
        }
    }

}
