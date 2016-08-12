/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

public class ProjectInfoTask extends DefaultTask {

    private String releaseVersion;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            LoggerUtil.lifecycle("Project version: "+releaseVersion);
            LoggerUtil.lifecycle("Project revision: "+RevisionUtil.getRevision(getProject()));
            GitUtil.generateCommitInfo(getProject());
            CommonUtil.printTeamcityInfo(getProject(), releaseVersion);
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
