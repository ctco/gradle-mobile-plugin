/*
 * @(#)KnappsackUploadTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.knappsack;

import lv.ctco.scm.gradle.TaskGroup;
import lv.ctco.scm.gradle.utils.ErrorUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class KnappsackUploadTask extends DefaultTask {

    private KnappsackExtension extension;

    public KnappsackUploadTask() {
        this.setGroup(TaskGroup.UPLOAD.getLabel());
        this.setDescription("Upload an artifact to a Knappsack server");
    }

    public void setExtension(KnappsackExtension extension) {
        this.extension = extension;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            KnappsackUtil.uploadArtifact(extension);
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
