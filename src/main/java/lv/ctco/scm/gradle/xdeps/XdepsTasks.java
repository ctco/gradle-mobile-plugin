/*
 * @(#)XdepsTasks.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.TaskGroup;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class XdepsTasks {

    private XdepsTasks() {}

    public static Task getOrCreateXdepsInfoTask(Project project, XdepsConfiguration xdepsConfiguration) {
        String taskName = "displayXdepsInfo";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            XdepsDisplayInfoTask newTask = project.getTasks().create(taskName, XdepsDisplayInfoTask.class);
            newTask.setGroup(TaskGroup.XDEPS.getLabel());
            newTask.setDescription("Displays Xdeps configuration information");
            newTask.setXdepsConfiguration(xdepsConfiguration);
            return newTask;
        } else {
            return existingTask;
        }
    }

    public static Task getOrCreatePublishXdepsTask(Project project) {
        String taskName = "publishXdeps";
        Task existingTask = project.getTasks().findByName(taskName);
        if (existingTask == null) {
            XdepsPublishTask newTask = project.getTasks().create(taskName, XdepsPublishTask.class);
            newTask.setGroup(TaskGroup.XDEPS.getLabel());
            newTask.setDescription("Publishes Xdeps publications to a Maven repository");
            return newTask;
        } else {
            return existingTask;
        }
    }

}
