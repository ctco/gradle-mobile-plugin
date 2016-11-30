/*
 * @(#)XamarinTasks.groovy
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Riga LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Environment;
import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

class XamarinTasks {

    private XamarinTasks() {}

    public static Task getOrCreateCleanTask(Project project) {
        Task task = getTaskByName(project, "clean");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DefaultTask, "clean") {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans project's build directories"
            }
        }
    }

    public static Task getOrCreateCleanIosTask(Project project, File slnFile) {
        Task task = getTaskByName(project, "cleanIos");
        if (task != null) {
            return task;
        } else {
            return project.task(type: CleanTask, "cleanIos") {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans project's build directories"
                solutionFile = slnFile
            }
        }
    }

    public static Task getOrCreateCleanAndroidTask(Project project, File slnFile) {
        Task task = getTaskByName(project, "cleanAndroid");
        if (task != null) {
            return task;
        } else {
            return project.task(type: CleanTask, "cleanAndroid") {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans project's build directories"
                solutionFile = slnFile
            }
        }
    }

    public static Task getOrCreateProjectInfoTask(Project project, String _releaseVersion) {
        Task task = getTaskByName(project, "projectInfo");
        if (task != null) {
            return task;
        } else {
            return project.task(type: ProjectInfoTask, "projectInfo") {
                group = TaskGroup.UTILITY.getLabel();
                description = "Prints project's configuration information";
                releaseVersion = _releaseVersion;
            }
        }
    }

    public static Task getOrCreateRestoreDependenciesTask(Project project) {
        Task task = getTaskByName(project, "restoreDependencies");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DefaultTask, "restoreDependencies") {
                group = TaskGroup.BUILD.getLabel()
                description = "Restores project's dependencies via NuGet restore"
            }
        }
    }

    public static Task getOrCreateRestoreDependenciesIosTask(Project project, File slnFile, File nugetConfigRoot) {
        Task task = getTaskByName(project, "restoreDependenciesForIos");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DependencyRestoreTask, "restoreDependenciesForIos") {
                group = TaskGroup.BUILD.getLabel()
                description = "Restores project's dependencies via NuGet restore"
                solutionFile = slnFile
                nugetPackagesConfigRootDir = nugetConfigRoot
            }
        }
    }

    public static Task getOrCreateRestoreDependenciesAndroidTask(Project project, File slnFile, File nugetConfigRoot) {
        Task task = getTaskByName(project, "restoreDependenciesForAndroid");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DependencyRestoreTask, "restoreDependenciesForAndroid") {
                group = TaskGroup.BUILD.getLabel()
                description = "Restores project's dependencies via NuGet restore"
                solutionFile = slnFile
                nugetPackagesConfigRootDir = nugetConfigRoot
            }
        }
    }

    public static Task getOrCreateBuildTask(Project project) {
        Task task = getTaskByName(project, "build");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DefaultTask, "build") {
                group = TaskGroup.BUILD.getLabel()
                description = "Runs builds for all project's target environments"
            }
        }
    }

    public static Task getOrCreateBuildIosTask(Project project) {
        Task task = getTaskByName(project, "buildIos");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DefaultTask, "buildIos") {
                group = TaskGroup.BUILD.getLabel()
                description = "Runs builds for all project's Ios target environments"
            }
        }
    }

    public static Task getOrCreateBuildAndroidTask(Project project) {
        Task task = getTaskByName(project, "buildAndroid");
        if (task != null) {
            return task;
        } else {
            return project.task(type: DefaultTask, "buildAndroid") {
                group = TaskGroup.BUILD.getLabel()
                description = "Runs builds for all project's Android target environments"
            }
        }
    }

    public static Task getOrCreateBuildIosEnvTask(Project project, Environment _env, XamarinExtension extXios) {
        Task task = getTaskByName(project, "build"+_env.getCamelName());
        if (task != null) {
            return task;
        } else {
            return project.task(type: BuildIosTask, "build"+_env.getCamelName()) {
                group = TaskGroup.BUILD.getLabel()
                description = "Builds iOS "+_env.getCamelName()+" environment with "+ _env.getConfiguration()+" configuration"
                env = _env
                solutionFile = extXios.solutionFile
                projectName = extXios.projectName
                assemblyName = extXios.assemblyName
            }
        }
    }

    public static Task getOrCreateProfileIosEnvTask(Project project, Environment _env, XamarinExtension extXios) {
        Task task = getTaskByName(project, "applyProfile"+_env.getCamelName());
        if (task != null) {
            return task;
        } else {
            return project.task(type: ProfilingTask, "applyProfile"+_env.getCamelName()) {
                description = "Profiles files for iOS "+ _env.getName()+" environment"
                projectName = extXios.projectName
                environmentName = _env.getName()
                profiles = extXios.getProfilesAsArray()
                enforcePlistSyntax = extXios.enforcePlistSyntax
            }
        }
    }

    public static Task getOrCreateUpdateVersionIosEnvTask(Project project, Environment _env, XamarinExtension extXios, String _releaseVersion) {
        Task task = getTaskByName(project, "updateVersion"+_env.getCamelName());
        if (task != null) {
            return task;
        } else {
            return project.task(type: UpdateVersionIosTask, "updateVersion$_env.camelName") {
                description = "Updates app version for iOS "+ _env.getName()+" environment"
                projectName = extXios.projectName
                environmentName = _env.getName()
                releaseVersion = _releaseVersion
                updateCFBundleShortVersionString = extXios.updateCFBundleShortVersionString
                cleanReleaseVersionForPROD = extXios.cleanReleaseVersionForPROD
                enforcePlistSyntax = extXios.enforcePlistSyntax
            }
        }
    }

    public static Task getOrCreateUnitTestTask(Project project, String _unitTestProject) {
        return project.task(type: UnitTestingTask, "runUnitTests") {
            group = TaskGroup.UTESTS.getLabel()
            description = "Runs unit tests for project"
            unitTestProject = _unitTestProject
        }
    }

    static Task getOrCreateUpdateVersionAndroidTask(Project project, Environment _env, XandroidExtension extXand, String _releaseVersion) {
        String taskName = "updateVersionAndroid"+StringUtils.capitalize(_env.getName().toLowerCase());
        Task task = project.getTasks().findByName(taskName);
        if (task != null) {
            return task;
        } else {
            return project.task(type: UpdateVersionAndroidTask, taskName) {
                description = "Updates version for Android "+ _env.getName()+" environment"
                projectName = extXand.projectName;
                releaseVersion = _releaseVersion;
                androidVersionCode = extXand.androidVersionCode;
            }
        }
    }

    public static Task getOrCreateIncrementVersionTask(Project project, XamarinExtension extXios, Csproj _csproj) {
        Task task = getTaskByName(project, "incrementProjectVersion")
        if (task != null) {
            return task
        } else {
            return project.task(type: IncrementProjectVersionTask, "incrementProjectVersion") {
                group = TaskGroup.UTILITY.getLabel()
                description = "Increments version in .sln and .csproj files";
                solutionFile = extXios.getSolutionFile();
                csproj = _csproj;
            }
        }
    }

    private static Task getTaskByName(Project project, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName);
            LoggerUtil.debug("Found existing task with name '"+taskName+"' and class '"+task.getClass().getCanonicalName()+"'");
            return task;
        } catch (UnknownTaskException ignore) {
            return null;
        }
    }

}
