/*
 * @(#)XdepsTasks.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class XdepsTasks {

    private XdepsTasks() {}

    public static Task getOrCreateXdepsDisplayInfoTask(Project project, XdepsConfiguration xdepsConfiguration) {
        String taskName = "displayXdepsInfo";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            XdepsDisplayInfoTask newTask = project.getTasks().create(taskName, XdepsDisplayInfoTask.class);
            newTask.setGroup(XdepsPlugin.XDEPS_TASK_GROUP);
            newTask.setDescription("Displays Xdeps configuration information");
            newTask.setXdepsConfiguration(xdepsConfiguration);
            return newTask;
        } else {
            return existingTask;
        }
    }

    public static Task getOrCreateXdepsPublishSnapshotsTask(Project project) {
        String taskName = "publishXdepsSnapshots";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            return createXdepsPublishTask(project, taskName, "MavenMobileSnapshots");
        } else {
            return existingTask;
        }
    }

    public static Task getOrCreateXdepsPublishReleasesTask(Project project) {
        String taskName = "publishXdepsReleases";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            return createXdepsPublishTask(project, taskName, "MavenMobileReleases");
        } else {
            return existingTask;
        }
    }

    private static Task createXdepsPublishTask(Project project, String taskName, String repoName) {
        XdepsPublishTask newTask = project.getTasks().create(taskName, XdepsPublishTask.class);
        newTask.setGroup(XdepsPlugin.XDEPS_TASK_GROUP);
        newTask.setDescription("Publishes Xdeps publications to '" + repoName + "' repository");
        newTask.setRepoName(repoName);
        return newTask;
    }

}
