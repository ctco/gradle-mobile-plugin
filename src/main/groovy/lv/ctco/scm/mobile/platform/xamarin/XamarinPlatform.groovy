/*
 * @(#)XamarinPlatform.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.objects.Environment
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.MultiTargetDetectorUtil
import lv.ctco.scm.mobile.core.utils.RevisionUtil;
import lv.ctco.scm.mobile.platform.common.CommonTasks;
import lv.ctco.scm.mobile.platform.common.UIAutomationTask;

import org.gradle.api.Project;
import org.gradle.api.Task;

class XamarinPlatform {

    public static final String NAME = "xamarin";

    protected Project project;

    private String releaseVersionIos;
    private String releaseVersionAndroid;
    private String revision;

    XamarinPlatform(Project project) {
        this.project = project;
    }

    /**
     * This is platform's entry point.
     *
     * It loads a solution file, performs automatic environment detection if required and then creates all tasks
     * required to build the project.
     *
     * @param extXios XamarinExtension instance from Gradle project.
     * @param extXand XandroidExtension instance from Gradle project.
     */
    void configure(XamarinExtension extXios, XandroidExtension extXand) {
        revision = RevisionUtil.getRevision(project);
        GitUtil.generateCommitInfo(project);

        if (extXios.solutionFile == null) {
            throw new IOException("solutionFile for ctcoMobile.xamarin extension is not defined")
        }

        if (extXios.solutionFile.exists()) {
            LoggerUtil.info("Loading solution file $extXios.solutionFile")
            SolutionParser sp = new SolutionParser(extXios.solutionFile)
            Solution solution = sp.parse()

            if (!solution.containsProject(extXios.projectName)) {
                throw new IOException("Project "+extXios.projectName+" does not exist in solution "+extXios.solutionFile)
            }

            SlnProjectSection sectionXios = solution.getProject(extXios.projectName)
            File csprojXiosFile = new File(extXios.solutionFile.parentFile.absolutePath, sectionXios.buildFilePath)
            Csproj csprojXios = new CsprojParser(csprojXiosFile).parse()

            if (extXios.automaticConfiguration) {
                LoggerUtil.lifecycle("Performing Xamarin iOS settings automatic configuration.")
                performAutomaticConfiguration(extXios, solution, sectionXios, csprojXios)
            }

            validateXamarinExtension(extXios)

            LoggerUtil.info("Final configuration:");
            LoggerUtil.info(extXios.toString());
            if (extXand.isValid()) {
                LoggerUtil.info(extXand.toString());
            } else {
                LoggerUtil.info("Valid Xamarin Android configuration not found.")
            }

            // Getting release versions from according csproj files
            releaseVersionIos = XamarinUtil.getReleaseVersion(csprojXios)
            if (extXand.isValid()) {
                LoggerUtil.info("Loading solution file $extXand.solutionFile")
                SolutionParser spXand = new SolutionParser(extXand.solutionFile)
                Solution slnXand = spXand.parse()
                if (!slnXand.containsProject(extXand.projectName)) {
                    throw new IOException("Project "+extXand.projectName+" does not exist in solution "+extXand.solutionFile)
                }
                SlnProjectSection sectionXand = solution.getProject(extXand.projectName)
                File csprojXandFile = new File(extXand.solutionFile.parentFile.absolutePath, sectionXand.buildFilePath)
                Csproj csprojXand = new CsprojParser(csprojXandFile).parse()
                releaseVersionAndroid = XamarinUtil.getReleaseVersion(csprojXand)
            }

            XamarinTasks.getOrCreateIncrementVersionTask(project, extXios, csprojXios)
            setupCleanTasks(extXios, extXand)
            setupProjectInfoTask(releaseVersionIos)
            setupBuildTasks(extXios, extXand)
            //setupUIATestTasks(extXios)
        } else {
            LoggerUtil.info("Defined solution file was not found! Will not configure build tasks...")
            LoggerUtil.info(extXios.toString())
        }
    }

    /**
     * Validates XamarinExtension and fails if any of the required fields is empty.
     *
     * @param extXios An extension to validate.
     * @throws IOException if there are validation errors
     */
    protected void validateXamarinExtension(XamarinExtension extXios) throws IOException {
        if (!extXios.isValid()) {
            throw new IOException("xamarin extension is invalid");
        }
    }

    /**
     * Creates ProjectInfoTask(name: projectInfo) the a project.
     *
     * @param csproj MS Build configuration to read version information from.
     */
    protected void setupProjectInfoTask(String releaseVersion) {
        XamarinTasks.getOrCreateProjectInfoTask(project, releaseVersion);
    }

    /**
     * Creates BuildIosTask(name: clean) that launches mdtool -t:clean target to clean up build directories.
     *
     * @param extXios XamarinExtension to read solutionFile location.
     * @param extXand XandroidExtension to read solutionFile location.
     */
    protected void setupCleanTasks(XamarinExtension extXios, XandroidExtension extXand) {
        Task cleanTask = XamarinTasks.getOrCreateCleanTask(project)
        Task cleanIos = XamarinTasks.getOrCreateCleanIosTask(project, extXios.getSolutionFile())
        cleanTask.dependsOn cleanIos
        if (extXand.isValid()) {
            Task cleanAnd = XamarinTasks.getOrCreateCleanAndroidTask(project, extXand.getSolutionFile())
            cleanTask.dependsOn cleanAnd
        }
    }

    /**
     * Creates BuildIosTask for every environment registered in the extension. Build tasks names are generated from
     * environment names (camel case). E.g. buildDev for DEV environment, buildTrain for TRAIN environment
     * and so on. Build tasks launch mdtool build -t:build -c:<env.configuration> <solution file name> so that
     * for every environment the whole solution is rebuilt and artifacts are copied to <ipaDistDir>.
     *
     * It also creates a "build" task that depends on all build* tasks.
     *
     * @param extXios XamarinExension to read configuration from.
     * @param extXand XandroidExension to read configuration from.
     */
    protected void setupBuildTasks(XamarinExtension extXios, XandroidExtension extXand) {
        Task buildTask = XamarinTasks.getOrCreateBuildTask(project)

        Task dependencyRestoreTask = XamarinTasks.getOrCreateRestoreDependenciesTask(project)
        Task dependencyRestoreIosTask = XamarinTasks.getOrCreateRestoreDependenciesIosTask(project, extXios.getSolutionFile(), extXios.getNugetPackagesConfigRootDir())
        dependencyRestoreTask.dependsOn(dependencyRestoreIosTask)

        Task buildIosTask = XamarinTasks.getOrCreateBuildIosTask(project)
        for (Environment _env : extXios.environments.values()) {
            Task envTask = XamarinTasks.getOrCreateBuildIosEnvTask(project, _env, extXios)
            Task profilingTask = XamarinTasks.getOrCreateProfileIosEnvTask(project, _env, extXios)
            Task updateVersionTask = XamarinTasks.getOrCreateUpdateVersionIosEnvTask(project, _env, extXios, releaseVersionIos)
            envTask.dependsOn(dependencyRestoreIosTask)
            if (extXios.skipUpdateVersionForAppstoreConfiguration && "AppStore|iPhone".equals(_env.getConfiguration())) {
                LoggerUtil.info("Disabling updateVersion task for $envTask.name as requested by configuration!")
            } else {
                envTask.dependsOn(updateVersionTask)
            }
            envTask.dependsOn(profilingTask)
            envTask.finalizedBy(CommonTasks.getOrCreateCleanupBuildTask(project, envTask.getName()))
            buildIosTask.dependsOn(envTask)
            buildTask.dependsOn(envTask)
            profilingTask.mustRunAfter(dependencyRestoreIosTask)
            updateVersionTask.mustRunAfter(dependencyRestoreIosTask)
        }

        Task unitTestTask = XamarinTasks.getOrCreateUnitTestTask(project, extXios.unitTestProject)
        unitTestTask.dependsOn(dependencyRestoreTask)

        if (extXand.isValid()) {
            Task buildAndroidTask = XamarinTasks.getOrCreateBuildAndroidTask(project)

            Task dependencyRestoreAndroidTask = XamarinTasks.getOrCreateRestoreDependenciesAndroidTask(project, extXand.getSolutionFile(), extXand.getNugetPackagesConfigRootDir())
            dependencyRestoreTask.dependsOn(dependencyRestoreAndroidTask)

            extXand.environments.values().each { Environment _env ->
                Task envTask = project.task type: BuildAndroidTask, "buildAndroid$_env.camelName", {
                    group = 'Mobile Build'
                    description = "Builds Android "+_env.getName()+" environment with "+_env.getConfiguration()+" configuration"
                    env = _env
                    projectFile = extXand.getProjectFile()
                    projectName = extXand.projectName
                    assemblyName = extXand.assemblyName
                    javaXmx = extXand.javaXmx
                    javaOpts = extXand.javaOpts
                    signingKeystore = extXand.signingKeystore
                    signingCertificateAlias = extXand.signingCertificateAlias
                }
                Task profilingTask = project.task type: ProfilingTask, "applyProfileAndroid$_env.camelName", {
                    description = "Profiles files for Android "+_env.getName()+" environment"
                    projectName = extXand.projectName
                    environmentName = _env.getName()
                    profiles = extXand.getProfilesAsArray()
                }
                Task manifestVersionUpdateTask = XamarinTasks.getOrCreateManifestVersionUpdateTask(project, extXand, releaseVersionAndroid)
                envTask.dependsOn(dependencyRestoreAndroidTask)
                envTask.dependsOn(manifestVersionUpdateTask)
                envTask.dependsOn(profilingTask)
                envTask.finalizedBy(CommonTasks.getOrCreateCleanupBuildTask(project, envTask.getName()))
                buildAndroidTask.dependsOn(envTask)
                buildTask.dependsOn(envTask)
                profilingTask.mustRunAfter(dependencyRestoreAndroidTask)
                manifestVersionUpdateTask.mustRunAfter(dependencyRestoreAndroidTask)
            }
        }
    }

    protected void processUIATestConfiguration(XamarinExtension extXios) {
        String fs = System.getProperty("file.separator")
        if (extXios.uiasetup.appPath == null) {
            if (extXios.projectName != null && extXios.uiasetup.appName != null && extXios.uiasetup.buildTarget != null) {
                extXios.uiasetup.appPath = new File(extXios.projectName + fs + "bin" + fs +
                        extXios.uiasetup.buildTarget.substring(
                                extXios.uiasetup.buildTarget.indexOf("|") + 1,
                                extXios.uiasetup.buildTarget.length()) + fs +
                        extXios.uiasetup.buildTarget.substring(0, extXios.uiasetup.buildTarget.indexOf("|")) + fs +
                        extXios.uiasetup.appName + ".app")
                LoggerUtil.info("Auto-calculated appPath as " + extXios.uiasetup.appPath)
            } else {
                throw new IOException("appPath parameter is not defined and can not be auto-calculated")
            }
        }
        if (extXios.uiasetup.appName == null) {
            extXios.uiasetup.appName = extXios.uiasetup.appPath.substring(
                    extXios.uiasetup.appName.lastIndexOf("/"),
                    extXios.uiasetup.appName.length()
            ).replace(".app", "")
            LoggerUtil.info("Auto-calculated appName as " + extXios.uiasetup.appName)
        }
        if (extXios.uiasetup.resultsPath == null) {
            extXios.uiasetup.resultsPath = new File(".")
        }
    }

    protected void setupUIATestTasks(XamarinExtension extXios) {
        Task testAllTask
        if (CommonTasks.hasUIATestConfiguration(extXios.uiasetup)) {
            LoggerUtil.info("UIAutomation test configuration was found. Configuring the UI testing tasks...")
            processUIATestConfiguration(extXios)
            testAllTask = project.task 'runUITests', {
                group = 'Mobile UI Test'
                description = "Runs all UI tests in $extXios.projectName project"
            }
            Task buildTask = project.task type: BuildIosTask, "buildUITestApp", {
                group = 'Mobile UI Test'
                description = "Builds app for testing with $extXios.uiasetup.buildTarget configuration"
                solutionFile = extXios.solutionFile
                configuration = extXios.uiasetup.buildTarget
                envCamelName = "UITestApp"
            }
            Task dependencyRestoreTask = XamarinTasks.getOrCreateRestoreDependenciesTask(project)
            buildTask.dependsOn dependencyRestoreTask

            testAllTask.dependsOn buildTask
            if (extXios.uiasetup.applyProfile != null) {
                String envCamelName = extXios.uiasetup.applyProfile[0].toUpperCase() +
                        extXios.uiasetup.applyProfile[1..extXios.uiasetup.applyProfile.length() - 1].toLowerCase()
                Task profilingTask = project.task type: ProfilingTask, overwrite: true, "applyProfile$envCamelName", {
                    description = "Profiles plist files for $extXios.uiasetup.applyProfile environment"
                    projectName = extXios.projectName
                    environmentName = extXios.uiasetup.applyProfile
                    profiles = extXios.getProfilesAsArray()
                    enforcePlistSyntax = extXios.enforcePlistSyntax
                }
                buildTask.dependsOn profilingTask
                buildTask.finalizedBy CommonTasks.getOrCreateCleanupBuildTask(project, buildTask.getName())
            }

            int t = 0
            for (String uiaTest : extXios.uiasetup.jsPath) {
                t++
                String uiTestNum = ("0" + t.toString()).substring(Math.max(0, ("0" + t.toString()).length() - 2))
                String uiTestName = (uiaTest.substring(uiaTest.lastIndexOf("/") + 1, uiaTest.length())).replace(".js", "")
                Task testTask = project.task type: UIAutomationTask, "runUITest" + uiTestNum + uiTestName, {
                    group = 'Mobile UI Test'
                    description = "Runs $uiTestName test"
                    testName = "UITest" + uiTestNum + uiTestName
                    appPath = new File(extXios.uiasetup.appPath)
                    resultsPath = new File(extXios.uiasetup.resultsPath)
                    targetDevice = extXios.uiasetup.targetDevice
                    jsPath = new File(uiaTest)
                }
                testTask.dependsOn buildTask
                testTask.finalizedBy(CommonTasks.getOrCreateCleanupSimulatorTask(project, testTask.getName()))
                testAllTask.dependsOn testTask
            }
        }
    }

    /**
     * Automatically configures environments based on solution configuration names.
     *
     * 1. Find all targets that follows the naming pattern: <projectBaseName> <environment name>|iPhone.
     * 2. If there is at leas one such target found, creates environments for all the found configurations.
     * 3. If there are no such configurations, it look for a target named <projectBaseName>|iPhone.
     * 4. If such configuration exists, it creates an environment for that target.
     * 5. If there are no such configuration, it look for a target named Ad-Hoc|iPhone.
     * 6. If such configuration exists, it creates an environment for that target.
     * 7. If there are no targets found, it just prints ot the warning message.
     *
     * @param extXios XamarinExtension to read configuration from
     * @param solution Solution file information
     * @param projectSection MS Build project to gather artifacts from
     * @param csproj MS Build project configuration
     */
    void performAutomaticConfiguration(XamarinExtension extXios, Solution solution,
                                       SlnProjectSection projectSection, Csproj csproj) {
        String projectDirectory = csproj.getDirectory().getAbsolutePath()
        extXios.assemblyName = csproj.getAssemblyName()

        LoggerUtil.info("Trying to determine project environments")
        MultiTargetDetectorUtil detector = new MultiTargetDetectorUtil("|iPhone")
        Set<String> solutionConfigurations = solution.solutionConfigurations
        HashMap<String, String> environments =
                detector.detectEnvironmentTargetsWithPrefix(extXios.projectBaseName,
                        new LinkedList<String>(solutionConfigurations))
        environments.each { key, value ->
            addEnvironment(key, value, projectSection, solution,
                    csproj, projectDirectory, extXios)
        }

        // Use Solution name or Ad-Hoc when there are no environment configurations
        if (extXios.environments.size() == 0) {
            if (solutionConfigurations.contains("$extXios.projectBaseName|iPhone".toString())) {
                addEnvironment('DEFAULT', "$extXios.projectBaseName|iPhone", projectSection, solution,
                        csproj, projectDirectory, extXios)

            } else if (solutionConfigurations.contains('Ad-Hoc|iPhone')) {
                addEnvironment('DEFAULT', 'Ad-Hoc|iPhone', projectSection, solution,
                        csproj, projectDirectory, extXios)
            } else if (solutionConfigurations.contains('Release|iPhone')) {
                addEnvironment('DEFAULT', 'Release|iPhone', projectSection, solution,
                        csproj, projectDirectory, extXios)
            } else {
                throw new IOException("No environments detected, no build is going to be performed!")
            }
        }
    }

    /**
     * Adds a new environment to the extension. It skips environment adding if there are environment with the
     * same name of configuration already defined in the extension.
     *
     * @param name Environment name
     * @param solutionConfiguration Solution configuration
     * @param project Project configuration
     * @param solution Solution
     * @param msBuildConfiguration MS Build project configuration
     * @param projectDirectory Directory of built project
     * @param extXios XamarinExtension to add environment to
     */
    protected void addEnvironment(String name, String solutionConfiguration, SlnProjectSection project,
                                  Solution solution, Csproj msBuildConfiguration,
                                  String projectDirectory, XamarinExtension extXios) {

        if (extXios.containsEnvironment(name)) {
            LoggerUtil.info("Detected environment $name is already defined, skipping")
            return
        }
        if (extXios.containsConfiguration(solutionConfiguration)) {
            LoggerUtil.info("Detected environment $name is mapped to configuration $solutionConfiguration, which" +
                    "is already defined, skipping")
            return
        }

        String projectConfigurationName = solution.getConfigurationMappingForProject(solutionConfiguration, project)
        String outputPathString = msBuildConfiguration.getOutputPathForConfiguration(projectConfigurationName)
        String outputPath = new File(projectDirectory, outputPathString).absolutePath
        extXios.environment name: name, configuration: solutionConfiguration, outputPath: outputPath
        LoggerUtil.info "Detected $name environment with configuration $solutionConfiguration and output path: $outputPath"
    }

}
