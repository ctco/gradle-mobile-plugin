/*
 * @(#)CommonTasks.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.common

import lv.ctco.scm.mobile.knappsack.KnappsackUploadTask
import lv.ctco.scm.mobile.knappsack.KnappsackUploadTaskConfigureAction

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException

class CommonTasks {

    private CommonTasks() {}

    /**
     * Registers a knappsackUpload task for project
     *
     * @deprecated
     * Use project.getPluginManager().apply(KnappsackPublishingPlugin.class) where needed instead.
     *
     * @param project Project to register the task for.
     */
    @Deprecated // (since = "0.15.4.0", forRemoval = true)
    static void registerKnappsackUploadTask(Project project) {
        Task task = getTaskByName(project, "knappsackUpload")
        if (task == null) {
            project.getTasks().register("knappsackUpload", KnappsackUploadTask.class, new KnappsackUploadTaskConfigureAction())
        }
    }

    static void registerReprofileIpaTask(Project project) {
        Task task = getTaskByName(project, "reprofileIpa")
        if (task == null) {
            project.getTasks().register("reprofileIpa", ReprofileIpaTask.class)
        }
    }

    static void registerReportGitCommitInfoTask(Project project) {
        Task task = getTaskByName(project, "reportGitCommitInfo")
        if (task == null) {
            project.getTasks().register("reportGitCommitInfo", ReportGitCommitInfoTask.class)
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
