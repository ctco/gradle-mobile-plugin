/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

public class ProjectInfoTask extends DefaultTask {

    private String releaseVersion;
    private String revision;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            LoggerUtil.lifecycle("Project version: "+releaseVersion);
            LoggerUtil.lifecycle("Project revision: "+revision);
            CommonUtil.printTeamcityInfo(getProject(), releaseVersion);
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
