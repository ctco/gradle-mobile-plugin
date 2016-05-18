/*
 * @(#)BaseExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects

import lv.ctco.scm.mobile.core.utils.LoggerUtil

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

import org.gradle.api.GradleException

abstract class PlatformExtension {

    String extensionClassName

    final Map<String, Environment> environments = new HashMap<String, Environment>()
    final Map<String, Profile> profiles = new HashMap<String, Profile>()

    UIAutomationSetup uiasetup

    /**
     * When true, the automatic configuration should be performed using the Xamarin project
     */
    boolean automaticConfiguration = true

    File solutionFile

    /**
     * Optional parameter that is required for auto-detection. When project name is not defined, the extension
     * form project name the following way: "${projectBaseName}.iOS"
     */
    String projectBaseName

    /**
     * Project name to build in solution.
     */
    String projectName

    /**
     * Assembly name of MSBuild project. Used to detect ipa archives. Set to project base name by default
     */
    String assemblyName = null

    /**
     * Enable verbose output for mdtool build. Disabled by default.
     * @Deprecated since version 0.14.+
     */
    @Deprecated
    boolean verbose = false

    /**
     * The path where to search for all packages.config to restore dependencies
     * restoreDependencies task will restore dependencies from solution file, if this is not set
     */
    String nugetPackagesConfigRootPath

    /**
     * The SVN URL override for which directory SVN revision should be gotten from.
     * If it's not set BundleVersionUpdateTask will use projectDir.
     * Deprecated since version 0.14.+
     */
    @Deprecated
    String svnRevisionRootUrl

    /**
     * Detected SVN revision to skip repeated look-ups
     * Deprecated since the implementation of RevisionInfoUtil Singleton in version 0.9.x
     */
    @Deprecated
    String svnRevision

    /**
     * Whether CFBundleVersion (Info.plist) and application_version (Root.plist) should be without SVN revision/GIT hash
     */
    boolean cleanReleaseVersionForPROD = false

    /*
    * Path to the project file to use for unit testing.
    */
    String unitTestProject

    PlatformExtension() {
        this.extensionClassName = this.getClass().getSimpleName().replaceAll("_Decorated", "")
    }

    public void setSolutionFile(File file) {
        if (projectBaseName == null) {
            setProjectBaseName(FilenameUtils.getBaseName(file.getName()))
        }
        this.solutionFile = file
    }

    public void setProjectBaseName(String name) {
        if (this.assemblyName == null) {
            this.assemblyName = name
        }
        if (projectName == null) {
            projectName = "${name}.iOS"
        }
        this.projectBaseName = name
    }

    public File getNugetPackagesConfigRootDir() {
        return nugetPackagesConfigRootPath == null ? null : new File(nugetPackagesConfigRootPath)
    }

    public boolean containsEnvironment(String name) {
        environments.count { key, value -> key.toLowerCase() == name.toLowerCase() } > 0
    }

    public boolean containsConfiguration(String name) {
        environments.count { key, value -> value.getConfiguration() == name } > 0
    }

    public void environment(Closure configuration) {
        Environment env = new Environment()
        configuration.setDelegate(env)
        configuration.setResolveStrategy(Closure.DELEGATE_FIRST)
        configuration.call()
        addEnvironment(env)
    }

    public void environment(HashMap<String, String> params) {
        Environment env = new Environment()
        env.setName(params.name)
        env.setConfiguration(params.configuration)
        if (params.outputPath.getClass().getName().endsWith('.File')) { // TODO : instanceof File works?
            env.setOutputPath((File)params.outputPath)
        } else {
            env.setOutputPath(new File(params.outputPath))
        }
        addEnvironment(env)
    }

    public void addEnvironment(Environment env) {
        if (StringUtils.isBlank(env.getName())) {
            throw new GradleException('Environment name is not defined')
        }
        if (StringUtils.isBlank(env.getConfiguration())) {
            throw new GradleException('Environment configuration is not defined')
        }
        if (env.getOutputPath() == null) {
            throw new GradleException('Environment output path is not defined')
        }
        if (containsEnvironment(env.getName())) {
            throw new GradleException("Environment "+env.getName()+" is already defined")
        }
        environments[env.getName()] = env
    }

    public void profile(Closure configuration) {
        Profile prof = new Profile()
        configuration.setDelegate(prof)
        configuration.setResolveStrategy(Closure.DELEGATE_FIRST)
        configuration.call()
        addProfile(prof)
    }

    public void profile(HashMap<String, String> params) {
        Profile profile = new Profile()
        // CHECK - param types
        profile.setEnvironment(params.environment)
        profile.setTarget(params.target)
        profile.setSources(params.sources)
        LoggerUtil.debug(params.environment+" scopes is "+params.scopes.getClass().getName())
        if (params.scopes != null) {
            profile.setScopes(params.scopes)
        }
        profile.additionalTargets = params.additionalTargets
        addProfile(profile)
    }

    public void addProfile(Profile profile) {
        if (StringUtils.isBlank(profile.getEnvironment())) {
            throw new GradleException('Profile environment is not defined')
        }
        if (StringUtils.isBlank(profile.getSources())) {
            throw new GradleException('Profile source is not defined')
        }
        /*
        if (profile.sources.endsWith('.tt')) {
            throw new GradleException('Profile source *.tt is not supported for Xcode')
        }
         */
        /*
        if (profile.getScopes() != null) {
            profile.setScopes(['build'])
        }
        */
        if (StringUtils.isBlank(profile.getTarget())) {
            if (!profile.getSources().toLowerCase().endsWith(".groovy")) {
                throw new GradleException('Profile target is not defined')
            }
        }
        profiles[profile.getEnvironment()+"|"+profile.getTarget()] = profile
    }

    public Profile[] getProfilesAsArray() {
        return profiles.values()
    }

    public void UIAutomationSetup(Closure configuration) {
        UIAutomationSetup uiaSetup = new UIAutomationSetup()
        configuration.setDelegate(uiaSetup)
        configuration.setResolveStrategy(Closure.DELEGATE_FIRST)
        configuration.call()
        addUIASetup(uiaSetup)
    }

    public void UIAutomationSetup(HashMap<String, String> params) {
        UIAutomationSetup uiaSetup = new UIAutomationSetup()
        uiaSetup.buildTarget = params.buildTarget
        uiaSetup.applyProfile = params.applyProfile
        uiaSetup.appName = params.appName
        uiaSetup.appPath = params.appPath
        uiaSetup.resultsPath = params.resultsPath
        uiaSetup.targetDevice = params.targetDevice
        uiaSetup.jsPath = params.jsPath
        addUIASetup(uiaSetup)
    }

    public void addUIASetup(UIAutomationSetup uiaSetup) {
        if (uiasetup == null) {
            uiasetup = uiaSetup
        } else {
            throw new GradleException("UI Automation Setup is already defined")
        }
    }

    @Override
    public String toString() {
        StringBuilder envString = new StringBuilder()
        environments.each {
            envString.append "    ${it.value},\n"
        }
        StringBuilder profString = new StringBuilder()
        profiles.each {
            profString.append "    ${it.value},\n"
        }
        Boolean plistSyntax = null
        Boolean skipUpdateVersion = null
        Boolean updateShortVersion = null
        if (extensionClassName.equals('XamarinExtension')) {
            plistSyntax = this.enforcePlistSyntax ? true : false
            skipUpdateVersion = this.skipUpdateVersionForAppstoreConfiguration ? true : false
            updateShortVersion = this.updateCFBundleShortVersionString ? true : false
        }
        return "$extensionClassName{\n" +
                "  automaticConfiguration="+getAutomaticConfiguration()+",\n" +
                "${solutionFile ? '  solutionFile=\'' + solutionFile + '\',\n' : ''}" +
                "${projectBaseName ? '  projectBaseName=\'' + projectBaseName + '\',\n' : ''}" +
                "${projectName ? '  projectName=\'' + projectName + '\',\n' : ''}" +
                "${assemblyName ? '  assemblyName=\'' + assemblyName + '\',\n' : ''}" +
                "${plistSyntax != null ? '  enforcePlistSyntax=' + plistSyntax + ',\n' : ''}" +
                "${skipUpdateVersion != null ? '  skipUpdateVersionForAppstoreConfiguration=' + skipUpdateVersion + ',\n' : ''}" +
                "${updateShortVersion != null ? '  updateCFBundleShortVersionString=' + updateShortVersion + ',\n' : ''}" +
                "${environments ? '  environments{\n' + envString.toString() + '  }\n' : '  environments{}\n'}" +
                "${profiles ? '  profiles{\n' + profString.toString() + '  }\n' : '  profiles{}\n'}" +
                "${uiasetup ? '  ' + uiasetup.toString() + '\n' : ''}" +
                '}';
    }

}
