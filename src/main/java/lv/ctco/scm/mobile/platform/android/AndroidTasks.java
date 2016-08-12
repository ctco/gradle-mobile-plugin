/*
 * @(#)AndroidTasks.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

import org.apache.commons.lang3.StringUtils;

import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

final class AndroidTasks {

    private AndroidTasks() {}

    static Task getOrCreateProjectInfoTask(Project project, String releaseVersion, String revision) {
        Task existingTask = getTaskByName(project, "projectInfo");
        if (existingTask == null) {
            ProjectInfoTask newTask = project.getTasks().create("projectInfo", ProjectInfoTask.class);
            newTask.setGroup("Z_"+TaskGroup.UTILITY.getLabel());
            newTask.setDescription("Prints project's configuration information");
            newTask.setReleaseVersion(releaseVersion);
            newTask.setRevision(revision);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateUpdateVersionAndroidTask(Project project, Environment env) {
        String taskNamePostfix = StringUtils.capitalize(env.getName().toLowerCase());
        Task existingTask = getTaskByName(project, "updateVersion"+taskNamePostfix);
        if (existingTask == null) {
            UpdateVersionAndroidTask newTask = project.getTasks().create("updateVersion"+taskNamePostfix, UpdateVersionAndroidTask.class);
            newTask.setGroup("Z_?");
            newTask.setDescription("!!! WIP !!!");
            newTask.setEnvironment(env);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getOrCreateArchiveArtifactTask(Project project, Environment env) {
        String taskNamePostfix = StringUtils.capitalize(env.getName().toLowerCase());
        Task existingTask = getTaskByName(project, "archiveArtifact"+taskNamePostfix);
        if (existingTask == null) {
            ArchiveArtifactTask newTask = project.getTasks().create("archiveArtifact"+taskNamePostfix, ArchiveArtifactTask.class);
            newTask.setGroup("Z_?");
            newTask.setDescription("!!! WIP !!!");
            newTask.setEnvironment(env);
            return newTask;
        } else {
            return existingTask;
        }
    }

    static Task getTaskByName(Project project, String taskName) {
        try {
            Task task = project.getTasks().getByName(taskName);
            LoggerUtil.debug("Found existing task with name '"+taskName+"' and class '"+task.getClass().getCanonicalName()+"'");
            return task;
        } catch (UnknownTaskException ignore) {
            return null;
        }
    }

}
