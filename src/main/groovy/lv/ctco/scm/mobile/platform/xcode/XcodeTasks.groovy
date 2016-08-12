/*
 * @(#)XcodeTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.objects.Environment;
import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

class XcodeTasks {

    private XcodeTasks() {}

    protected static Task getOrCreateCleanTask(Project project) {
        Task task = getTaskByName(project, "clean")
        if (task != null) {
            return task
        } else {
            return project.task("clean", type: CleanTask) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans up (restores backuped files and deletes known build directories)"
            }
        }
    }

    protected static Task getOrCreateProjectInfoTask(Project project, XcodeExtension ext) {
        Task task = getTaskByName(project, "projectInfo")
        if (task != null) {
            return task
        } else {
            return project.task("projectInfo", type: ProjectInfoTask) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Prints project's configuration information"
                libraryVersion = ext.getLibraryVersion()
            }
        }
    }

    protected static Task getOrCreateRestoreDependenciesTask(Project project) {
        Task task = getTaskByName(project, "restoreDependencies")
        if (task != null) {
            return task
        } else {
            return project.task("restoreDependencies", type: DependencyRestoreTask) {
                group = TaskGroup.BUILD.getLabel()
                description = "Restores \'xdeps\' configuration\'s dependencies to Libraries/ folder"
            }
        }
    }

    protected static Task getOrCreateBuildTask(Project project) {
        Task task = getTaskByName(project, "build")
        if (task != null) {
            return task
        } else {
            return project.task("build") {
                group = TaskGroup.BUILD.getLabel()
                description = 'Builds all project\'s environments'
            }
        }
    }

    protected static Task getOrCreateBuildEnvTask(Project project, Environment _env) {
        Task task = getTaskByName(project, "build"+_env.getCamelName())
        if (task != null) {
            return task
        } else {
            return project.task("build"+_env.getCamelName(), type: BuildTask) {
                group = TaskGroup.BUILD.getLabel()
                description = "Builds \'"+_env.getName()+"\' environment with \'"+_env.getTarget()+"\' target"
                env = _env
            }
        }
    }

    protected static Task getOrCreateUpdateVersionTask(Project project, Environment env) {
        Task task = getTaskByName(project, "updateVersion"+env.getCamelName())
        if (task != null) {
            return task
        } else {
            return project.task("updateVersion"+env.getCamelName(), type: UpdateVersionIosTask) {
                //group = TaskGroup.BUILD.getLabel()
                description = "Updates app version for "+env.getName()+" environment"
                targetName = env.getTarget()
            }
        }
    }

    protected static Task getOrCreateProfilingTask(Project project, Environment env, XcodeExtension ext) {
        Task task = getTaskByName(project, "applyProfile"+env.getCamelName())
        if (task != null) {
            return task
        } else {
            return project.task("applyProfile"+env.getCamelName(), type: ProfilingTask) {
                //group = TaskGroup.BUILD.getLabel()
                description = "Profiles files for "+env.getName()+" environment"
                envName = env.getName()
                profiles = ext.getProfilesAsArray()
            }
        }
    }

    protected static Task getOrCreateArchiveLibrariesTask(Project project) {
        Task task = getTaskByName(project, "archiveLibraries")
        if (task != null) {
            return task
        } else {
            return project.task("archiveLibraries", type: LibraryArchiveTask) {
                group = TaskGroup.PUBLISH.getLabel()
                description = "Create library archives from build artifact directories"
            }
        }
    }

    protected static Task getOrCreatePublishLibrariesTask(Project project) {
        Task task = getTaskByName(project, "publishLibraries")
        if (task != null) {
            return task
        } else {
            return project.task("publishLibraries", type: LibraryPublishTask) {
                group = TaskGroup.PUBLISH.getLabel()
                description = "Publish created artifact archives to defined Maven repository"
            }
        }
    }

    protected static Task getOrCreateRunUnitTestsTask(Project project, String _schemeName) {
        Task task = getTaskByName(project, "runUnitTests")
        if (task != null) {
            return task
        } else {
            return project.task("runUnitTests", type: UnitTestingTask) {
                group = TaskGroup.UTESTS.getLabel()
                description = "Runs unit tests using a defined scheme"
                schemeName = _schemeName
            }
        }
    }

    private static Task getTaskByName(Project project, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName)
            LoggerUtil.debug("Found existing task with name '"+taskName+"' and class '"+task.getClass().getCanonicalName()+"'")
            return task
        } catch (UnknownTaskException ignore) {
            return null
        }
    }

}
