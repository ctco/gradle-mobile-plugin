/*
 * @(#)CreateTagTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.common;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.StampUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class CreateTagTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(CreateTagTask.class);

    private static final String PROP_STAMP = "stamp";

    @TaskAction
    public void doTaskAction() {
        try {
            String stamp;
            if (PropertyUtil.hasProjectProperty(getProject(), PROP_STAMP)) {
                stamp = PropertyUtil.getProjectProperty(getProject(), PROP_STAMP);
            } else {
                throw new IOException("Property '"+PROP_STAMP+"' has not been provided");
            }
            String newStamp = StampUtil.generateNewStampVersion(stamp);
            logger.info("New stamp version is '{}'", newStamp);
            TeamcityUtil.setProjectParameter(PROP_STAMP, newStamp);
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
