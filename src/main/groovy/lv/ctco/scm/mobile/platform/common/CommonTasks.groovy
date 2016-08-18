/*
 * @(#)CommonTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common

import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.utils.BackupUtil;
import lv.ctco.scm.mobile.core.utils.IosSimulatorUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUploadTask
import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

public class CommonTasks {

    private CommonTasks() {}

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
                description = "Calculates and prints new stamp from provided 'changeset' and 'stamp'"
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
                extension = KnappsackUtil.setupKnappsackExtension(project)
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
