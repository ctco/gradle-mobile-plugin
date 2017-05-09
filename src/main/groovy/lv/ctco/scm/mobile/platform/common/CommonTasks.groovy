/*
 * @(#)CommonTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common

import lv.ctco.scm.mobile.core.objects.TaskGroup

import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUploadTask
import lv.ctco.scm.mobile.infrastructure.knappsack.KnappsackUtil

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException

class CommonTasks {

    private CommonTasks() {}

    static Task getOrCreateCreateTagTask(Project project) {
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

    static Task getOrCreateKnappsackUploadTask(Project project) {
        Task task = getTaskByName(project, "knappsackUpload")
        if (task != null) {
            return task
        } else {
            return project.task("knappsackUpload", type: KnappsackUploadTask) {
                group = TaskGroup.UPLOAD.getLabel()
                description = "Upload an artifact to a Knappsack server"
                extension = KnappsackUtil.setupKnappsackExtension(project)
            }
        }
    }

    static Task getOrCreateIpaReprofilingTask(Project project) {
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

    static Task getOrCreateReportGitCommitInfoTask(Project project) {
        Task task = getTaskByName(project, "reportGitCommitInfo")
        if (task != null) {
            return task
        } else {
            return project.task("reportGitCommitInfo", type: ReportGitCommitInfoTask) {
                group = TaskGroup.UTILITY.getLabel()
                description = "Creates a HTML report about the commit"
            }
        }
    }

    private static Task getTaskByName(Project project, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName)
            return task
        } catch (UnknownTaskException ignore) {
            return null
        }
    }

}
