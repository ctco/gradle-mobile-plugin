/*
 * @(#)XdepsPublishRules.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import org.gradle.api.Task;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;

public class XdepsPublishRules extends RuleSource {

    @Mutate
    public static void linkPublishingTasks(ModelMap<Task> tasks) {
        for (XdepsPublishTask xdepsPublishTask : tasks.withType(XdepsPublishTask.class)) {
            for (Task publishTask : tasks.withType(PublishToMavenRepository.class)) {
                if (publishTask.getName().endsWith("PublicationTo" + xdepsPublishTask.getRepoName() + "Repository")) {
                    xdepsPublishTask.dependsOn(publishTask);
                    publishTask.setGroup(null);
                }
            }
        }
    }

}
