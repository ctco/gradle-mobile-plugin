/*
 * @(#)XcodePlatform.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import lv.ctco.scm.mobile.core.objects.Environment
import lv.ctco.scm.mobile.core.utils.LoggerUtil
import lv.ctco.scm.mobile.core.utils.MultiTargetDetectorUtil
import lv.ctco.scm.mobile.core.utils.PathUtil
import lv.ctco.scm.mobile.platform.common.CommonTasks
import lv.ctco.scm.mobile.platform.common.UIAutomationTask

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task

class XcodePlatform {

    public static final NAME = "xcode"

    protected Project project

    XcodePlatform(Project project) {
        this.project = project
    }

    public void configure(XcodeExtension ext) {
        if (ext.getAutomaticConfiguration()) {
            LoggerUtil.debug("Performing Xcode settings automatic configuration")
            performAutomaticConfiguration(ext)
        }
        configureBuildTasks(ext)
        configureUtilityTasks(ext)
        configureUnitTestTasks(ext)
        configureUiaTestTasks(ext)
        configureLibraryPublications(ext)

        LoggerUtil.info("Final configuration:")
        LoggerUtil.info(ext.toString())
        if (ext.uiasetup != null) {
            LoggerUtil.info(ext.uiasetup.toString())
        }
        if (!LibraryUtil.getLibrariesPublications(project).isEmpty()) {
            LibraryUtil.printLibrariesRepositoriesInfo(project)
        }
    }

    /**
     * Reads Xcode project settings and tries to configure the environment list based on pre-defined configuration
     * naming conventions.
     *
     * 1. If the first configuration does not follow the <project name> <environment name> rule, then the project is
     * considered to be a single configuration project and only a single environment "Default" with the default configuration name
     * is configured.
     * 2. If the first configuration follows the <project name> <environment name> rule, then the method searches for other
     * configuration that follow the same rule and have the same <project name> part. All these targets are considered to
     * be parts of a multi-configuration build.
     * 3. If there is only one such configuration the build is considered to be a single-configuration builds and is handled the same
     * way as the 1. case.
     * 4. If there is more that one such configuration the build is considered to be a multi-configuration build and new
     * environments are crated for each configuration. Environment have the <environment name> name.
     *
     * If an environment is detected and the ext already has an environment with the same name, it is ignored and
     * the process continues. It makes it possible to redefine some environments that do not follow the standard
     * naming convention.
     *
     * @param ext XcodeExtension where environments should be created.
     */
    void performAutomaticConfiguration(XcodeExtension ext) {
        LoggerUtil.info('Trying to determine project type (single target | multi-target)')
        List<String> targets = XcodeUtil.getTargets()
        String defaultTarget = XcodeUtil.getDefaultTarget()
        LoggerUtil.debug("Default target is ["+defaultTarget+"]")

        HashMap<String, String> environments = new MultiTargetDetectorUtil().detectEnvironmentTargets(defaultTarget, targets)
        if (environments.size() > 1) {
            LoggerUtil.info("Multi-target project with ${environments.size()} environments detected.")
            environments.each { String environmentName, String targetName ->
                if (!ext.containsEnvironment(environmentName) && !ext.containsTarget(targetName)) {
                    LoggerUtil.info("Adding $environmentName environment with target '$targetName'")
                    ext.environment name: environmentName, target: targetName
                } else {
                    LoggerUtil.info("Environment $environmentName skipped, because it is already configured" +
                            " and will not be overriden.")
                }
            }
        } else {
            LoggerUtil.info("Single target project detected, because the default target name '$defaultTarget'" +
                    " does not match the multi-target project naming convention.")
            if (!ext.containsEnvironment('DEFAULT') && !ext.containsTarget(defaultTarget)) {
                ext.environment(name: 'DEFAULT', target: defaultTarget)
            }
        }
    }

    private void configureBuildTasks(XcodeExtension ext) {
        Task dependencyRestoreTask = XcodeTasks.getOrCreateRestoreDependenciesTask(project)
        Task buildAllTask = XcodeTasks.getOrCreateBuildTask(project)

        for (Environment env : ext.getEnvironments().values()) {
            Task buildEnvTask = XcodeTasks.getOrCreateBuildEnvTask(project, env)
            Task buildEnvUpdateVersionTask = XcodeTasks.getOrCreateUpdateVersionTask(project, env)
            Task buildEnvProfileTask = XcodeTasks.getOrCreateProfilingTask(project, env, ext)

            buildEnvTask.dependsOn(dependencyRestoreTask)
            buildEnvTask.dependsOn(buildEnvProfileTask)
            buildEnvTask.dependsOn(buildEnvUpdateVersionTask)
            buildEnvTask.finalizedBy(CommonTasks.getOrCreateCleanupBuildTask(project, buildEnvTask.getName()))

            buildEnvProfileTask.mustRunAfter(dependencyRestoreTask)
            buildEnvUpdateVersionTask.mustRunAfter(buildEnvProfileTask)

            buildAllTask.dependsOn(buildEnvTask)
        }
    }

    private void configureUtilityTasks(XcodeExtension ext) {
        XcodeTasks.getOrCreateCleanTask(project)
        XcodeTasks.getOrCreateProjectInfoTask(project, ext)
    }

    private void configureUnitTestTasks(XcodeExtension ext) {
        Task testTask = XcodeTasks.getOrCreateRunUnitTestsTask(project, ext.getUnitTestScheme())
        testTask.dependsOn(XcodeTasks.getOrCreateRestoreDependenciesTask(project))
        // TODO : Fix Simulator controls
        //testTask.finalizedBy(CommonTasks.getOrCreateCleanupSimulatorTask(project, testTask.getName()))
    }

    private void configureUiaTestTasks(XcodeExtension _ext) {
        Task testAllTask
        if (CommonTasks.hasUIATestConfiguration(_ext.uiasetup)) {
            processUIATestConfiguration(_ext)

            Environment uitestEnv = new Environment()
            uitestEnv.setTarget(_ext.uiasetup.buildTarget)
            uitestEnv.setName("UIA-TEST")
            uitestEnv.setConfiguration("Release")
            uitestEnv.setSdk("iphonesimulator")

            testAllTask = project.task 'runUITests', {
                group = 'Mobile UI Test'
                description = "Runs all UI tests in project"
            }
            Task buildTestAppTask = project.task("buildUITestApp", type: BuildTask) {
                group = 'Mobile UI Test'
                description = "Builds UI test environment with '"+uitestEnv.getTarget()+"' target"
                env = uitestEnv
            }
            buildTestAppTask.dependsOn(XcodeTasks.getOrCreateRestoreDependenciesTask(project))
            buildTestAppTask.finalizedBy CommonTasks.getOrCreateCleanupBuildTask(project, buildTestAppTask.getName())
            testAllTask.dependsOn buildTestAppTask

            int t = 0
            for (String uiaTest : _ext.uiasetup.jsPath) {
                t++
                String uiTestNum = ("0" + t.toString()).substring(Math.max(0, ("0" + t.toString()).length() - 2))
                String uiTestName = (uiaTest.substring(uiaTest.lastIndexOf("/") + 1, uiaTest.length())).replace(".js", "")
                Task testTask = project.task type: UIAutomationTask, "runUITest" + uiTestNum + uiTestName, {
                    group = 'Mobile UI Test'
                    description = "Runs $uiTestName test"
                    testName = "UITest" + uiTestNum + uiTestName
                    appPath = new File(_ext.uiasetup.appPath)
                    resultsPath = new File(_ext.uiasetup.resultsPath)
                    targetDevice = _ext.uiasetup.targetDevice
                    jsPath = new File(uiaTest)
                }
                testTask.dependsOn buildTestAppTask
                testTask.finalizedBy(CommonTasks.getOrCreateCleanupSimulatorTask(project, testTask.getName()))
                testAllTask.dependsOn testTask
            }
        }
    }

    protected void processUIATestConfiguration(XcodeExtension _ext) {
        if (_ext.uiasetup.appPath == null) {
            if (_ext.uiasetup.appName != null && _ext.uiasetup.buildTarget != null) {
                _ext.uiasetup.appPath = new File(PathUtil.getXcodeSymDir(), "UIA-TEST/Release-iphonesimulator/"+_ext.uiasetup.appName+".app")
                LoggerUtil.info("Auto-calculated appPath as " + _ext.uiasetup.appPath)
            } else {
                throw new GradleException("appPath parameter is not defined and can not be auto-calculated")
            }
        }
        if (_ext.uiasetup.appName == null) {
            _ext.uiasetup.appName = _ext.uiasetup.appPath.substring(
                    _ext.uiasetup.appName.lastIndexOf("/"),
                    _ext.uiasetup.appName.length()
            ).replace(".app", "")
            LoggerUtil.info("Auto-calculated appName as " + _ext.uiasetup.appName)
        }
        if (_ext.uiasetup.resultsPath == null) {
            _ext.uiasetup.resultsPath = new File(".")
        }
    }

    private void configureLibraryPublications(XcodeExtension ext) {
        if (!LibraryUtil.getLibrariesPublications(project).isEmpty()) {
            Task libraryArchiveTask = XcodeTasks.getOrCreateArchiveLibrariesTask(project)
            Task libraryPublishTask = XcodeTasks.getOrCreatePublishLibrariesTask(project)
            libraryPublishTask.dependsOn(libraryArchiveTask)
            LibraryUtil.configureSingleMavenLibraryRepository(project, "mobile"+XcodeUtil.getXcodeLibraryPublishRepoType(ext.getLibraryVersion()))
        }
    }

}
