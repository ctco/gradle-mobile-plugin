/*
 * @(#)CommonTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.common

import lv.ctco.scm.mobile.knappsack.KnappsackUploadTask
import lv.ctco.scm.mobile.knappsack.KnappsackUtil

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException

class CommonTasks {

    private CommonTasks() {}

    static Task getOrCreateKnappsackUploadTask(Project project) {
        Task task = getTaskByName(project, "knappsackUpload")
        if (task != null) {
            return task
        } else {
            return project.task("knappsackUpload", type: KnappsackUploadTask) {
                extension = KnappsackUtil.setupKnappsackExtension(project)
            }
        }
    }

    static Task getOrCreateIpaReprofilingTask(Project project) {
        Task task = getTaskByName(project, "reprofileIpa")
        if (task != null) {
            return task
        } else {
            return project.task("reprofileIpa", type: ReprofileIpaTask)
        }
    }

    static Task getOrCreateReportGitCommitInfoTask(Project project) {
        Task task = getTaskByName(project, "reportGitCommitInfo")
        if (task != null) {
            return task
        } else {
            return project.task("reportGitCommitInfo", type: ReportGitCommitInfoTask)
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
