/*
 * @(#)XcodeExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode

import groovy.transform.TypeChecked

import lv.ctco.scm.mobile.utils.Profile

@TypeChecked
class XcodeExtension {

    private XcodeConfiguration configuration = new XcodeConfiguration()

    boolean automaticConfiguration = true
    File projectFile
    String projectName
    String unitTestScheme

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
        env.setTarget(params.target)
        if (params.configuration != null) {
            env.setConfiguration(params.configuration)
        }
        if (params.sdk != null) {
            env.setSdk(params.sdk)
        }
        configuration.addEnvironment(env)
    }

    void profile(Closure closure) {
        Profile profile = new Profile()
        closure.setDelegate(profile)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addProfile(profile)
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

    XcodeConfiguration getXcodeConfiguration() {
        configuration.automaticConfiguration = automaticConfiguration
        if (projectFile != null) {
            configuration.setProjectFile(projectFile)
        }
        if (projectName != null) {
            configuration.setProjectName(projectName)
        }
        if (unitTestScheme != null) {
            configuration.setUnitTestScheme(unitTestScheme)
        }
        return configuration
    }

}
