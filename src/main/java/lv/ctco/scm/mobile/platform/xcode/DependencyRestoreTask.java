/*
 * @(#)DependencyRestoreTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.DependencyUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class DependencyRestoreTask extends DefaultTask {

    @TaskAction
    public void doTaskAction() {
        try {
            DependencyUtil.restoreXdepsDependencies(getProject());
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
