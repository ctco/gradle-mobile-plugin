/*
 * @(#)KnappsackUploadTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.infrastructure.knappsack;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class KnappsackUploadTask extends DefaultTask {

    private KnappsackExtension extension;

    public void setExtension(KnappsackExtension extension) {
        this.extension = extension;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            KnappsackUtil.uploadArtifact(extension);
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
