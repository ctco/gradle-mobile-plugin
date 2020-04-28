/*
 * @(#)XamarinTasks.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.gradle.TaskGroup;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

final class XamarinTasks {

    private XamarinTasks() {}

    static Task getOrCreateCleanTask(Project project) {
        String taskName = "clean";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DefaultTask newTask = project.getTasks().create(taskName, DefaultTask.class);
            newTask.setGroup(TaskGroup.UTILITY.getLabel());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateCleanIosTask(Project project, File sln) {
        String taskName = "cleanIos";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            CleanTask newTask = project.getTasks().create(taskName, CleanTask.class);
            // Do not set group so the task will be nicely sorted in the 'tasks -all' output
            newTask.setDescription("Reverts applied profiling and deletes known build directories");
            newTask.setSolutionFile(sln);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateCleanAndroidTask(Project project, File sln) {
        String taskName = "cleanAndroid";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            CleanTask newTask = project.getTasks().create(taskName, CleanTask.class);
            // Do not set group so the task will be nicely sorted in the 'tasks -all' output
            newTask.setDescription("Reverts applied profiling and deletes known build directories");
            newTask.setSolutionFile(sln);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateProjectInfoTask(Project project, String releaseVersion,
                                           XamarinConfiguration iosConf, XandroidConfiguration andConf) {
        String taskName = "projectInfo";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            ProjectInfoTask newTask = project.getTasks().create(taskName, ProjectInfoTask.class);
            newTask.setGroup(TaskGroup.UTILITY.getLabel());
            newTask.setDescription("Prints project's configuration information");
            newTask.setReleaseVersion(releaseVersion);
            newTask.setXamarinConfiguration(iosConf);
            newTask.setXandroidConfiguration(andConf);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateRevertProfileTask(Project project, String envName) {
        String taskName = "revertProfile"+StringUtils.capitalize(envName);
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            RevertProfileTask newTask = project.getTasks().create(taskName, RevertProfileTask.class);
            newTask.setGroup(TaskGroup.UTILITY.getLabel());
            newTask.setDescription("Reverts applied profiling");
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateRestoreDependenciesTask(Project project) {
        String taskName = "restoreDependencies";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DefaultTask newTask = project.getTasks().create(taskName, DefaultTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateRestoreDependenciesIosTask(Project project, File sln) {
        String taskName = "restoreDependenciesForIos";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DependencyRestoreTask newTask = project.getTasks().create(taskName, DependencyRestoreTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            newTask.setDescription("Restores project's dependencies via NuGet restore");
            newTask.setSolutionFile(sln);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateRestoreDependenciesAndroidTask(Project project, File sln) {
        String taskName = "restoreDependenciesForAndroid";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DependencyRestoreTask newTask = project.getTasks().create(taskName, DependencyRestoreTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            newTask.setDescription("Restores project's dependencies via NuGet restore");
            newTask.setSolutionFile(sln);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateBuildTask(Project project) {
        String taskName = "build";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DefaultTask newTask = project.getTasks().create(taskName, DefaultTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateBuildIosTask(Project project) {
        String taskName = "buildIos";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DefaultTask newTask = project.getTasks().create(taskName, DefaultTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            newTask.setDescription("Builds project's iOS target environments");
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateBuildAndroidTask(Project project) {
        String taskName = "buildAndroid";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            DefaultTask newTask = project.getTasks().create(taskName, DefaultTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            newTask.setDescription("Builds project's iOS target environments");
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateBuildIosEnvTask(Project project, Environment env, XamarinConfiguration iosConf) {
        String taskName = "build"+StringUtils.capitalize(env.getName());
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            BuildIosTask newTask = project.getTasks().create(taskName, BuildIosTask.class);
            newTask.setGroup(TaskGroup.BUILD.getLabel());
            newTask.setDescription("Builds iOS "+env.getName()+" environment with "+env.getConfiguration()+" configuration");
            newTask.setEnv(env);
            newTask.setSolutionFile(iosConf.getSolutionFile());
            newTask.setProjectFile(iosConf.getProjectFile());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateUpdateVersionIosEnvTask(Project project, Environment env, XamarinConfiguration extXios, String releaseVersion) {
        String taskName = "updateVersion"+StringUtils.capitalize(env.getName());
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            UpdateVersionIosTask newTask = project.getTasks().create(taskName, UpdateVersionIosTask.class);
            newTask.setDescription("Updates app version for iOS "+env.getName()+" environment");
            newTask.setEnvironmentName(env.getName());
            newTask.setProjectFile(extXios.getProjectFile());
            newTask.setReleaseVersion(releaseVersion);
            newTask.setCleanReleaseVersionForPROD(extXios.isCleanReleaseVersionForPROD());
            newTask.setUpdateCFBundleShortVersionString(extXios.isUpdateCFBundleShortVersionString());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateUpdateVersionAndroidTask(Project project, Environment env, XandroidConfiguration extXand, String releaseVersion) {
        String taskName = "updateVersionAndroid"+StringUtils.capitalize(env.getName());
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            UpdateVersionAndroidTask newTask = project.getTasks().create(taskName, UpdateVersionAndroidTask.class);
            newTask.setDescription("Updates app version for Android "+env.getName()+" environment");
            newTask.setProjectFile(extXand.getProjectFile());
            newTask.setReleaseVersion(releaseVersion);
            newTask.setAndroidVersionCode(extXand.getAndroidVersionCode());
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateRunUnitTestsTask(Project project, String unitTestProject) {
        String taskName = "runUnitTests";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            UnitTestingTask newTask = project.getTasks().create(taskName, UnitTestingTask.class);
            newTask.setGroup(TaskGroup.TESTS.getLabel());
            newTask.setDescription("Runs unit tests for project");
            newTask.setUnitTestProject(unitTestProject);
            return newTask;
        } else {
            return existingTask;
        }
    }

}
