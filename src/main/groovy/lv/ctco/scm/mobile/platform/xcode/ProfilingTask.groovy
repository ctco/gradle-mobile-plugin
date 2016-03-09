/*
 * @(#)ProfilingTask.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.objects.Profile;
import lv.ctco.scm.mobile.core.utils.GroovyProfilingUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.ProfilingUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class ProfilingTask extends DefaultTask {

    public String envName;
    public Profile[] profiles;

    @TaskAction
    public void doTaskAction() {
        for (Profile profile : profiles) {
            if (envName.equals(profile.getEnvironment()) && profile.hasScope("build")) {
                for (String pathSource : profile.getSources().split(",")) {
                    File profileFile = new File(pathSource);
                    String profileSourceType = profileFile.getName().toLowerCase();
                    if (!profileFile.exists()) {
                        stopWithException(profileFile.getAbsolutePath()+" does not exist");
                    }

                    File targetFile = null;
                    if (!pathSource.toLowerCase().endsWith(".groovy")) {
                        targetFile = new File(profile.getTarget());
                        if (!targetFile.exists()) {
                            stopWithException(targetFile.getAbsolutePath()+" does not exist");
                        }
                    }

                    if (profileSourceType.endsWith(".groovy")) {
                        GroovyProfilingUtil.profileUsingGroovyEval(profileFile);
                    } else if (profileSourceType.endsWith(".tt")) {
                        stopWithException("Unsupported profiling type!");
                    } else {
                        try {
                            ProfilingUtil.profileUsingPlistEntries(targetFile, profileFile);
                        } catch (IOException e) {
                            stopWithException(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void stopWithException(String message) {
        LoggerUtil.errorInTask(this.getName(), message);
        throw new GradleException(message);
    }

}
