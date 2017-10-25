/*
 * @(#)XdepsPublishTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class XdepsPublishTask extends DefaultTask {

    private static final String ERR_NO_PUBLICATIONS = "No xdeps publication definitions have been found";
    private static final String ERR_NO_REPOSITORIES = "No xdeps publication repositories have been found";

    @TaskAction
    public void doTaskAction() {
        if (XdepsUtil.getMavenPublications(getProject()).isEmpty()) {
            ErrorUtil.errorInTask(getName(), ERR_NO_PUBLICATIONS);
            throw new GradleException(ERR_NO_PUBLICATIONS);
        }
        if (XdepsUtil.getMavenRepositories(getProject()).isEmpty()) {
            ErrorUtil.errorInTask(getName(), ERR_NO_REPOSITORIES);
            throw new GradleException(ERR_NO_REPOSITORIES);
        }
    }

}
