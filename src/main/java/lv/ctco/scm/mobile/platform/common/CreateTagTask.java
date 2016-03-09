/*
 * @(#)CreateTagTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.StampUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class CreateTagTask extends DefaultTask {

    @TaskAction
    public void doTaskAction() {
        String stamp;
        if (PropertyUtil.hasProjectProperty("stamp")) {
            stamp = PropertyUtil.getProjectProperty("stamp");
        } else {
            String error = "Property stamp has not been provided!";
            LoggerUtil.errorInTask(this.getName(), error);
            throw new GradleException(error);
        }
        String newStamp;
        try {
            newStamp = StampUtil.generateNewStampVersion(stamp);
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
        LoggerUtil.info("New stamp version is: "+newStamp);
        TeamcityUtil.setProjectParameter("stamp", newStamp);
    }

}
