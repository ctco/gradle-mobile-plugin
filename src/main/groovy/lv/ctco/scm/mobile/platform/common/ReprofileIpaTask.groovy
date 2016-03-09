/*
 * @(#)ReprofileIpaTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common

import lv.ctco.scm.mobile.core.objects.Profile
import lv.ctco.scm.mobile.core.utils.LoggerUtil
import lv.ctco.scm.mobile.core.utils.PropertyUtil
import lv.ctco.scm.mobile.core.utils.ReprofilingUtil

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ReprofileIpaTask extends DefaultTask {

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    public void doTaskAction() {
        checkProvidedParameters()
        String platform = project.ctcoMobile.platform
        LoggerUtil.debug('Reading profile configuration for ['+platform+'] platform')
        Profile[] profiles
        boolean cleanReleaseVersion = false
        if (platform.equals('xcode')) {
            profiles = project.ctcoMobile.xcode.getProfilesAsArray()
        } else if (platform.equals('xamarin')) {
            profiles = project.ctcoMobile.xamarin.getProfilesAsArray()
            cleanReleaseVersion = project.ctcoMobile.xamarin.cleanReleaseVersionForPROD
        } else {
            throw new GradleException('Unsupported platform!')
        }
        String targetEnvName = PropertyUtil.getProjectProperty("reprofiling.environment")
        File targetIpaFile = new File(PropertyUtil.getProjectProperty("reprofiling.artifact"))
        if (targetEnvName == null || targetEnvName.isEmpty()) {
            throw new GradleException('Reprofiling target environment name has not been provided!')
        }
        if (!targetIpaFile.getName().toLowerCase().endsWith('.ipa')) {
            throw new GradleException('Non-IPA reprofiling target artifact has been provided!')
        }
        if (!targetIpaFile.exists()) {
            throw new GradleException('Reprofiling target artifact has not been found!')
        }
        LoggerUtil.info('Reprofiling IPA file...')
        ReprofilingUtil.reprofileIpa(targetIpaFile, targetEnvName, profiles, cleanReleaseVersion)
        LoggerUtil.info('Reprofiling IPA done.')
    }

    private static void checkProvidedParameters() {
        if (!(PropertyUtil.hasProjectProperty("reprofiling.artifact") && PropertyUtil.hasProjectProperty("reprofiling.environment"))) {
            throw new GradleException("Required parameters have not been provided!")
        }
    }

}
