/*
 * @(#)XcodeExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import lv.ctco.scm.mobile.core.objects.Environment
import lv.ctco.scm.mobile.core.objects.PlatformExtension
import lv.ctco.scm.mobile.core.objects.Profile
import lv.ctco.scm.mobile.core.utils.CommonUtil

class XcodeExtension extends PlatformExtension {

    String libraryGroupId

    String libraryVersion

    @Deprecated
    boolean skipLibraryPublish = false

    /** When true, the automatic configuration should be performed using the Xcode project. */
    boolean automaticConfiguration = true

    /** SDK to build an application. */
    String sdk = "iphoneos"

    /** Project configuration to build. */
    String configuration = "Release"

    /* Name of the scheme to use for unit testing. */
    String unitTestScheme

    public boolean containsEnvironment(String name) {
        environments.count { key, value -> key.toLowerCase() == name.toLowerCase() } > 0
    }

    public boolean containsTarget(String name) {
        environments.count { key, value -> value.getTarget() == name } > 0
    }

    public void environment(HashMap<String, String> params) {
        Environment env = new Environment()
        env.setName(params.name)
        if (params.target) {
            env.setTarget(params.target)
        }
        addEnvironment(env)
    }

    public void addEnvironment(Environment env) {
        if (CommonUtil.isBlank(env.getName())) {
            throw new IOException('Environment name is not defined')
        }
        if (CommonUtil.isBlank(env.getTarget())) {
            throw new IOException('Environment target is not defined')
        }
        if (containsEnvironment(env.getName())) {
            throw new IOException("Environment "+env.getName()+" is already defined")
        }
        if (CommonUtil.isBlank(env.getConfiguration())) {
            env.setConfiguration(configuration)
        }
        if (CommonUtil.isBlank(env.getSdk())) {
            env.setSdk(sdk)
        }
        environments[env.getName()] = env
    }

    public void addProfile(Profile profile) {
        if (CommonUtil.isBlank(profile.getEnvironment())) {
            throw new IOException('Profile environment is not defined')
        }
        if (CommonUtil.isBlank(profile.getSources())) {
            throw new IOException('Profile source is not defined')
        }
        if (profile.getSources().endsWith(".tt")) {
            throw new IOException('Profile source *.tt is not supported for Xcode')
        }
        /*
        if (profile.getScopes() != null) {
            profile.scopes = ["build"]
        }
        */
        if (CommonUtil.isBlank(profile.getTarget()) && !profile.getSources().toLowerCase().endsWith(".groovy")) {
            throw new IOException('Profile target is not defined')
        }
        profiles[profile.getEnvironment()+"|"+profile.getTarget()] = profile
    }

}
