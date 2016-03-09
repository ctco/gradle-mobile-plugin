/*
 * @(#)LibraryPublishTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class LibraryPublishTask extends DefaultTask {

    private static final String ERR_NO_PUBLICATIONS = "No library publication definitions have been found";
    private static final String ERR_NO_REPOSITORIES = "No library publication definitions have been found";

    @TaskAction
    public void doTaskAction() {
        if (LibraryUtil.getLibrariesPublications(getProject()).isEmpty()) {
            LoggerUtil.errorInTask(getName(), ERR_NO_PUBLICATIONS);
            throw new GradleException(ERR_NO_PUBLICATIONS);
        }
        if (LibraryUtil.getLibrariesRepositories(getProject()).isEmpty()) {
            LoggerUtil.errorInTask(getName(), ERR_NO_REPOSITORIES);
            throw new GradleException(ERR_NO_REPOSITORIES);
        }
    }

}
