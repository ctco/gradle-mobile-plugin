/*
 * @(#)XdepsPublishTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public class XdepsPublishTask extends DefaultTask {

    @Internal
    private String repoName;

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    @TaskAction
    public void doTaskAction() {
        if (!XdepsUtil.hasMavenPublications(getProject())) {
            ErrorUtil.errorInTask(getName(), "Missing Maven publication definitions");
        }
        if (!XdepsUtil.hasMavenRepository(getProject(), repoName)) {
            ErrorUtil.errorInTask(getName(), "Missing '"+repoName+"' repository definition");
        }
    }

}
