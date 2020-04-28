/*
 * @(#)XcodePlatform.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode

import groovy.transform.TypeChecked

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

import org.gradle.api.Project
import org.gradle.api.Task

@TypeChecked
class XcodePlatform {

    void configure(Project project, XcodeConfiguration configuration) {
        if (XcodeUtil.getXcodeprojCount(project.projectDir) != 1) {
            return
        }

        if (configuration.getProjectFile() == null) {
            configuration.setProjectFile(XcodeUtil.getXcodeprojFile(project.projectDir))
        }

        if (configuration.getProjectName() == null) {
            configuration.setProjectName(FilenameUtils.getBaseName(configuration.getProjectFile().getName()))
        }

        if (configuration.getEnvironments().isEmpty() && configuration.isAutomaticConfiguration()) {
            performAutomaticEnvironmentConfiguration(configuration)
        }

        configureBuildTasks(project, configuration)
        configureUtilityTasks(project, configuration)
        configureUnitTestTasks(project, configuration)
    }

    void performAutomaticEnvironmentConfiguration(XcodeConfiguration configuration) {
        String defaultTarget = XcodeUtil.getDefaultTarget()
        List<String> allTargets = XcodeUtil.getTargets()
        List<Environment> environments = XcodeUtil.getAutodetectedEnvironments(defaultTarget, allTargets)
        if (environments.isEmpty()) {
            throw new IOException("Failed to autodetect environments")
        } else {
            for (Environment env : environments) {
                configuration.addEnvironment(env)
            }
        }
    }

    private void configureBuildTasks(Project project, XcodeConfiguration configuration) {
        Task dependencyRestoreTask = XcodeTasks.getOrCreateRestoreDependenciesTask(project)
        Task buildAllTask = XcodeTasks.getOrCreateBuildTask(project)

        for (Environment env : configuration.getEnvironments()) {
            Task buildEnvTask = XcodeTasks.getOrCreateBuildEnvTask(project, env)
            Task buildEnvUpdateVersionTask = XcodeTasks.getOrCreateUpdateVersionTask(project, env)
            Task buildEnvApplyProfileTask = getOrCreateProfilingTask(project, env, configuration)

            buildEnvTask.dependsOn(dependencyRestoreTask)
            buildEnvTask.dependsOn(buildEnvApplyProfileTask)
            buildEnvTask.dependsOn(buildEnvUpdateVersionTask)
            buildEnvTask.finalizedBy(XcodeTasks.getOrCreateRevertProfileTask(project, env.getName()))

            buildEnvApplyProfileTask.mustRunAfter(dependencyRestoreTask)
            buildEnvUpdateVersionTask.mustRunAfter(buildEnvApplyProfileTask)

            buildAllTask.dependsOn(buildEnvTask)
        }
    }

    private void configureUtilityTasks(Project project, XcodeConfiguration configuration) {
        XcodeTasks.getOrCreateCleanTask(project)
        XcodeTasks.getOrCreateProjectInfoTask(project, configuration)
    }

    private void configureUnitTestTasks(Project project, XcodeConfiguration configuration) {
        Task testTask = XcodeTasks.getOrCreateRunUnitTestsTask(project, configuration.getUnitTestScheme())
        testTask.dependsOn(XcodeTasks.getOrCreateRestoreDependenciesTask(project))
    }

    private Task getOrCreateProfilingTask(Project project, Environment env, XcodeConfiguration configuration) {
        String taskName = "applyProfile"+StringUtils.capitalize(env.getName())
        Task existingTask = project.getTasks().findByName(taskName)
        if (existingTask == null) {
            ProfilingTask newTask = project.getTasks().create(taskName, ProfilingTask.class)
            newTask.setDescription("Profiles files for '"+env.getName()+"' environment")
            newTask.setProjectDir(new File(configuration.getProjectName()))
            newTask.setProfiles(configuration.getSpecificProfiles(env.getName(), "build"))
            return newTask
        } else {
            return existingTask
        }
    }

}
