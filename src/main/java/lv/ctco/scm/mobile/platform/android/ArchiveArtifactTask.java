/*
 * @(#)ArchiveArtifactTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class ArchiveArtifactTask extends DefaultTask {

    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            if (getSignedArtifactPath().exists()) {
                FileUtils.copyFile(getSignedArtifactPath(), getArchiveArtifactPath());
            } else if (getUnsignedArtifactPath().exists()) {
                FileUtils.copyFile(getUnsignedArtifactPath(), getArchiveArtifactPath());
            } else {
                throw new IOException("Expected APK was not found in build directory");
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private File getSignedArtifactPath() {
        return new File(environment.getAssemblyPath(),
                environment.getAssemblyName()+"-"+environment.getConfiguration()+"."+environment.getAssemblyType());
    }

    private File getUnsignedArtifactPath() {
        return new File(environment.getAssemblyPath(),
                environment.getAssemblyName()+"-"+environment.getConfiguration()+"-unsigned."+environment.getAssemblyType());
    }

    private File getArchiveArtifactPath() throws IOException {
        String standartizedArtifactName = environment.getAssemblyName()+"-"+environment.getBuildVersion()+"-"+environment.getConfiguration()+"."+environment.getAssemblyType();
        if ("apk".equals(environment.getAssemblyType())) {
            return new File(PathUtil.getApkDistDir(), standartizedArtifactName);
        } else if ("aar".equals(environment.getAssemblyType())) {
            return new File(PathUtil.getAarDistDir(), standartizedArtifactName);
        } else {
            throw new IOException("Unsupported environment type");
        }
    }

}
