/*
 * @(#)XandroidExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Profile

class XandroidExtension {

    private XandroidConfiguration configuration = new XandroidConfiguration()

    boolean automaticConfiguration = false
    File solutionFile
    File projectFile
    String projectName
    String unitTestProject

    String signingKeystore
    String signingCertificateAlias
    String androidVersionCode

    void environment(Closure closure) {
        Environment env = new Environment()
        closure.setDelegate(env)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addEnvironment(env)
    }

    void environment(HashMap<String, String> params) {
        Environment env = new Environment()
        env.setName(params.name)
        if (params.configuration != null) {
            env.setConfiguration(params.configuration)
        }
        env.setPlatform(params.platform)
        configuration.addEnvironment(env)
    }

    void profile(Closure closure) {
        Profile prof = new Profile()
        closure.setDelegate(prof)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addProfile(prof)
    }

    void profile(HashMap<String, String> params) {
        Profile profile = new Profile()
        profile.setEnvironment(params.environment)
        profile.setTarget(params.target)
        profile.setSource(params.source)
        if (params.scope != null) {
            profile.setScope(params.scope)
        }
        if (params.order != null) {
            profile.setOrder(Integer.parseInt(params.order))
        }
        if (params.level != null) {
            profile.setLevel(Integer.parseInt(params.level))
        }
        configuration.addProfile(profile)
    }

    XandroidConfiguration getXandroidConfiguration() {
        configuration.setAutomaticConfiguration(automaticConfiguration)
        if (solutionFile != null) {
            configuration.setSolutionFile(solutionFile)
        }
        if (projectFile != null) {
            configuration.setProjectFile(projectFile)
        }
        if (projectName != null) {
            configuration.setProjectName(projectName)
        }
        if (unitTestProject != null) {
            configuration.setUnitTestProject(unitTestProject)
        }
        configuration.setSigningCertificateAlias(signingCertificateAlias)
        configuration.setSigningKeystore(signingKeystore)
        configuration.setAndroidVersionCode(androidVersionCode)
        return configuration
    }

}
