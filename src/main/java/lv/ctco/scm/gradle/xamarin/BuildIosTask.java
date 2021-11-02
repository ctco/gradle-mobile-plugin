/*
 * @(#)BuildIosTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.mobile.utils.IosApp;
import lv.ctco.scm.mobile.utils.BuildReportUtil;
import lv.ctco.scm.mobile.utils.CommonUtil;
import lv.ctco.scm.gradle.utils.ErrorUtil;
import lv.ctco.scm.mobile.utils.PathUtil;
import lv.ctco.scm.mobile.utils.ZipUtil;
import lv.ctco.scm.utils.exec.ExecCommand;
import lv.ctco.scm.utils.exec.ExecResult;
import lv.ctco.scm.utils.exec.ExecUtil;
import lv.ctco.scm.utils.exec.LoggerOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BuildIosTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(BuildIosTask.class);

    private Environment env;
    private File solutionFile;
    private File projectFile;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    private String getProjectName() {
        return projectFile.getParentFile().getName();
    }

    private File getConfigurationBinDir() {
        return new File(getProjectName()+"/bin/"+env.getPlatform()+"/"+env.getConfiguration());
    }

    @TaskAction
    public void doTaskAction() {
        try {
            buildArtifacts();
            checkArtifacts();
            moveArtifactsToDistDir();
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private void buildArtifacts() throws IOException {
        ExecCommand execCommand = new ExecCommand("msbuild");
        execCommand.addArgument("/property:Configuration="+env.getConfiguration());
        if (env.getPlatform() != null) {
            execCommand.addArgument("/property:Platform="+env.getPlatform());
        }
        execCommand.addArgument("/target:Build");
        execCommand.addArgument(solutionFile.getAbsolutePath(), false);
        execCommand.addArgument("/consoleLoggerParameters:NoSummary");
        logger.debug("{}", execCommand);
        ExecResult execResult = ExecUtil.executeCommand(execCommand, new LoggerOutputStream());
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

    private void checkArtifacts() throws IOException {
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(getConfigurationBinDir());
        if (apps.size() == 1) {
            appDir = apps.get(0);
        } else {
            throw new GradleException("None or multiple apps found in build directory");
        }

        IosApp iosApp = new IosApp(appDir);
        iosApp.setName(env.getName());
        iosApp.setBuildCnf(env.getConfiguration());
        iosApp.setBuildSdk(env.getPlatform());
        BuildReportUtil.addIosAppInfo(iosApp);
    }

    private void moveArtifactsToDistDir() throws IOException {
        File ipa;
        List<File> ipas = CommonUtil.findIosIpasInDirectory(getConfigurationBinDir());
        if (ipas.size() == 1) {
            ipa = ipas.get(0);
        } else {
            throw new GradleException("None or multiple IPAs found!");
        }
        String ipaName = FilenameUtils.getBaseName(ipa.getName());
        if (!StringUtils.endsWithIgnoreCase(ipaName, env.getName().toUpperCase())) {
            ipaName = getProjectName()+" "+env.getName().toUpperCase();
        }
        FileUtils.copyFile(ipa, new File(PathUtil.getIpaDistDir(), ipaName+".ipa"));
        FileUtils.forceDelete(ipa);
        //
        List<File> dsyms = CommonUtil.findIosDsymsinDirectory(getConfigurationBinDir());
        if (dsyms.size() == 1 ) {
            File dsymDir = dsyms.get(0);
            File dsymDistDir = new File(getProject().getBuildDir(), "dsymdist");
            ZipUtil.compressDirectory(dsymDir, true, new File(dsymDistDir, getProjectName()+" "+env.getName()+".dSYM.zip"));
            FileUtils.deleteDirectory(dsymDir);
        } else {
            logger.warn("None or multiple DSYMs found! Not moving to distribution folder.");
        }
    }

}
