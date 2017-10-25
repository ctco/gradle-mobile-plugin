/*
 * @(#)RevertProfileTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.mobile.utils.BackupUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class RevertProfileTask extends DefaultTask {

    @TaskAction
    public void doTaskAction() {
        try {
            BackupUtil.restoreAllFiles();
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
