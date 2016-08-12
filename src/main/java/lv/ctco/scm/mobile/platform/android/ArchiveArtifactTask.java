/*
 * @(#)ArchiveArtifactTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import org.apache.commons.io.FileUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;

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
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
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
