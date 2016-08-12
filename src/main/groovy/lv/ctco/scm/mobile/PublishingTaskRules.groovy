/*
 * @(#)PublishingTaskRules.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import org.gradle.api.Task;
import org.gradle.model.RuleSource;
import org.gradle.model.Mutate;
import org.gradle.model.ModelMap;

class PublishingTaskRules extends RuleSource {

    @Mutate
    public static void linkPublishingTasks(ModelMap<Task> tasks) {
        Task libraryPublishTask = tasks.get("publishLibraries");
        if (libraryPublishTask != null) {
            for (Task task : tasks) {
                if (task.name.startsWith("publishLibrary")
                        && (task.name.endsWith("PublicationToMobile-snapshotsRepository")
                        || task.name.endsWith("PublicationToMobile-releasesRepository")) ) {
                    libraryPublishTask.dependsOn(task);
                    task.setGroup(null);
                }
            }
        }
    }

}
