/*
 * @(#)XamarinExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Profile

class XamarinExtension {

    private XamarinConfiguration configuration = new XamarinConfiguration()

    boolean automaticConfiguration = true
    File solutionFile
    File projectFile
    String projectName
    String unitTestProject

    @Deprecated
    boolean cleanReleaseVersionForPROD = false
    @Deprecated
    boolean updateCFBundleShortVersionString = false
    @Deprecated
    boolean skipUpdateVersionForAppstoreConfiguration = false

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
        env.setConfiguration(params.configuration)
        env.setPlatform(params.platform)
        configuration.addEnvironment(env)
    }

    public void profile(Closure closure) {
        Profile prof = new Profile()
        closure.setDelegate(prof)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()
        configuration.addProfile(prof)
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

    XamarinConfiguration getXamarinConfiguration() {
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
        configuration.setCleanReleaseVersionForPROD(cleanReleaseVersionForPROD)
        configuration.setUpdateCFBundleShortVersionString(updateCFBundleShortVersionString)
        configuration.setSkipUpdateVersionForAppstoreConfiguration(skipUpdateVersionForAppstoreConfiguration)
        return configuration
    }

}
