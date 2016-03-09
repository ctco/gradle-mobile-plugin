/*
 * @(#)BuildIosTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.objects.Environment;
import lv.ctco.scm.mobile.core.objects.IosApp;
import lv.ctco.scm.mobile.core.utils.BuildReportUtil;
import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.ZipUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BuildIosTask extends DefaultTask {

    private Environment env;
    private File solutionFile;
    private String projectName;
    private String assemblyName;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            buildArtifacts();
            checkArtifacts();
            moveArtifactsToDistDir();
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void buildArtifacts() throws IOException {
        CommandLine commandLine = new CommandLine("/Applications/Xamarin Studio.app/Contents/MacOS/mdtool");
        commandLine.addArgument("build");
        commandLine.addArgument("-t:Build");
        if (StringUtils.isNotBlank(projectName)) {
            commandLine.addArgument("-p:"+projectName);
        }
        if (StringUtils.isNotBlank(env.getConfiguration())) {
            commandLine.addArgument("-c:"+env.getConfiguration());
        }
        commandLine.addArgument(solutionFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
        FileUtils.writeLines(new File(PathUtil.getBuildlogDir(), this.getName()+"Task.build.log"), execResult.getOutput());
        if (!execResult.isSuccess()) {
            LoggerUtil.errorInTask(this.getName(), execResult.getException().getMessage());
            throw new GradleException("Artifact build failed");
        }
    }

    private void checkArtifacts() throws IOException {
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(env.getOutputPath());
        if (apps.size() == 1) {
            appDir = apps.get(0);
        } else {
            throw new GradleException("None or multiple apps found in build directory");
        }

        IosApp iosApp = new IosApp(appDir, env);
        BuildReportUtil.addIosAppInfo(iosApp);
    }

    private void moveArtifactsToDistDir() throws IOException {
        File ipa;
        List<File> ipas = CommonUtil.findIosIpasInDirectory(env.getOutputPath());
        if (ipas.size() == 1) {
            ipa = ipas.get(0);
        } else {
            throw new GradleException("None or multiple IPAs found!");
        }
        String ipaName = FilenameUtils.getBaseName(ipa.getName());
        if (!StringUtils.endsWithIgnoreCase(ipaName, env.getUpperCaseName())) {
            ipaName = assemblyName+" "+env.getUpperCaseName();
        }
        FileUtils.copyFile(ipa, new File(PathUtil.getIpaDistDir(), ipaName+".ipa"));
        FileUtils.forceDelete(ipa);
        //
        List<File> dsyms = CommonUtil.findIosDsymsinDirectory(env.getOutputPath());
        if (dsyms.size() == 1 ) {
            File dsymDir = dsyms.get(0);
            ZipUtil.compressDirectory(dsymDir, true, new File(PathUtil.getDsymDistDir(), "dSYM."+env.getName()+".zip"));
            FileUtils.deleteDirectory(dsymDir);
        } else {
            LoggerUtil.warn("None or multiple DSYMs found! Not moving to distribution folder.");
        }
    }

}
