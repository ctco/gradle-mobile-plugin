/*
 * @(#)XcodeExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import lv.ctco.scm.mobile.core.objects.Profile

class XcodeExtension {

    private XcodeConfiguration configuration = new XcodeConfiguration()

    boolean automaticConfiguration = true
    File projectFile
    String projectName
    String unitTestScheme
    String libraryGroupId
    String libraryVersion

    public void environment(Closure closure) {
        Environment env = new Environment()
        closure.setDelegate(env)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addEnvironment(env)
    }

    public void environment(HashMap<String, String> params) {
        Environment env = new Environment()
        env.setName(params.name)
        env.setTarget(params.target)
        configuration.addEnvironment(env)
    }

    public void profile(Closure closure) {
        Profile profile = new Profile()
        closure.setDelegate(profile)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addProfile(profile)
    }

    public void profile(HashMap<String, String> params) {
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
        if (libraryGroupId != null) {
            configuration.setLibraryGroupId(libraryGroupId)
        }
        if (libraryVersion != null) {
            configuration.setLibraryVersion(libraryVersion)
        }
        return configuration
    }

}
