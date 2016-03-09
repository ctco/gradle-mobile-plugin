/*
 * @(#)CommonTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common

import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.objects.UIAutomationSetup;
import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.IosSimulatorUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUploadTask
import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;

@Singleton
public class CommonTasks {

    public static boolean hasUIATestConfiguration(UIAutomationSetup uiaSetup) {
        boolean result = true
        if (uiaSetup == null) {
            result = false
        } else {
            if (uiaSetup.buildTarget == null) {
                result = false
            }
            if (uiaSetup.jsPath == null) {
                result = false
            }
        }
        return result
    }

    public static Task getOrCreateCleanupSimulatorTask(Project project, String taskName) {
        String taskNamePostfix = taskName[0].toUpperCase() + taskName[1..taskName.length() - 1]
        Task task = getTaskByName(project, "cleanup$taskNamePostfix")
        if (task != null) {
            return task
        } else {
            return project.task("cleanup$taskNamePostfix", type: DefaultTask,) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans up after $taskName task"
                doFirst {
                    IosSimulatorUtil.quitDefaultSimulator();
                }
            }
        }
    }

    public static Task getOrCreateCleanupBuildTask(Project project, String taskName) {
        String taskNamePostfix = taskName[0].toUpperCase()+taskName[1..taskName.length()-1]
        Task task = getTaskByName(project, "cleanup$taskNamePostfix")
        if (task != null) {
            return task
        } else {
            return project.task("cleanup$taskNamePostfix", type: DefaultTask,) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Cleans up after $taskName task (restores backuped plists)"
                doFirst {
                    BackupUtil.restoreAllFiles()
                }
            }
        }
    }

    public static Task getOrCreateCreateTagTask(Project project) {
        Task task = getTaskByName(project, "createTag")
        if (task != null) {
            return task
        } else {
            return project.task("createTag", type: CreateTagTask) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Calculates and prints new stamp from provided \'changeset\' and \'stamp\'"
            }
        }
    }

    public static Task getOrCreateKnappsackUploadTask(Project project) {
        Task task = getTaskByName(project, "knappsackUpload")
        if (task != null) {
            return task
        } else {
            return project.task("knappsackUpload", type: KnappsackUploadTask) {
                group = TaskGroup.KNAPPSACK.getLabel()
                description = "Upload an artifact to a Knappsack server"
                extension = KnappsackUtil.setupKnappsackExtension()
            }
        }
    }

    public static Task getOrCreateIpaReprofilingTask(Project project) {
        Task task = getTaskByName(project, "reprofileIpa")
        if (task != null) {
            return task
        } else {
            return project.task("reprofileIpa", type: ReprofileIpaTask) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Re-profiles and re-signs an IPA file to other environment"
            }
        }
    }

    public static Task createTarSourcesTask(Project project) {
        String TAR_TASK_PROP = 'runTarSources'
        String SOURCES = 'sources.tgz'

        return project.task("tarSources", type: Tar, overwrite: true) {
            group = TaskGroup.UTILITY.getLabel()
            description = 'Compresses projects source files into sources.tgz'
            onlyIf {
                def sourceFile = new File(SOURCES)
                def rtaskBoolean = true

                if (project.hasProperty(TAR_TASK_PROP)) {
                    if (project.property(TAR_TASK_PROP) == "false") {
                        rtaskBoolean = false
                        LoggerUtil.info("Project has property: " + TAR_TASK_PROP + "skipping task")
                    }
                }
                if (sourceFile.exists()) {
                    LoggerUtil.info(SOURCES + " exists skipping task")
                    rtaskBoolean = false
                }

                return rtaskBoolean
            }
            from('.')
            exclude('.git')
            exclude('.svn')
            exclude('.gradle')
            exclude('build')
            exclude(SOURCES)
            compression = Compression.GZIP
            archiveName = SOURCES
        }
    }

    private static Task getTaskByName(Project project, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName)
            LoggerUtil.debug("Found existing task with name ["+taskName+"] and class ["+task.getClass().getCanonicalName()+"]")
            return task
        } catch (UnknownTaskException ignore) {
            return null
        }
    }

}
