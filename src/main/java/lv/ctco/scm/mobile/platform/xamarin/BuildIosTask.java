/*
 * @(#)BuildIosTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import lv.ctco.scm.mobile.core.objects.IosApp;
import lv.ctco.scm.mobile.core.utils.BuildReportUtil;
import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.ExecResult;
import lv.ctco.scm.mobile.core.utils.ExecUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.ZipUtil;

import org.apache.commons.exec.CommandLine;
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
    private String projectName;

    private String configurationBinPath;

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            configurationBinPath = projectName+"/bin/"+env.getPlatform()+"/"+env.getConfiguration();
            buildArtifacts();
            checkArtifacts();
            moveArtifactsToDistDir();
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
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
            commandLine.addArgument("-c:"+env.getConfiguration()+"|"+env.getPlatform());
        }
        commandLine.addArgument(solutionFile.getAbsolutePath(), false);
        ExecResult execResult = ExecUtil.execCommand(commandLine, null, null, true, true);
        FileUtils.writeLines(new File(PathUtil.getBuildlogDir(), this.getName()+"Task.build.log"), execResult.getOutput());
        if (!execResult.isSuccess()) {
            ErrorUtil.errorInTask(this.getName(), execResult.getException());
        }
    }

    private void checkArtifacts() throws IOException {
        File appDir;
        List<File> apps = CommonUtil.findIosAppsInDirectory(new File(configurationBinPath));
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
        List<File> ipas = CommonUtil.findIosIpasInDirectory(new File(configurationBinPath));
        if (ipas.size() == 1) {
            ipa = ipas.get(0);
        } else {
            throw new GradleException("None or multiple IPAs found!");
        }
        String ipaName = FilenameUtils.getBaseName(ipa.getName());
        if (!StringUtils.endsWithIgnoreCase(ipaName, env.getName().toUpperCase())) {
            ipaName = projectName+" "+env.getName().toUpperCase();
        }
        FileUtils.copyFile(ipa, new File(PathUtil.getIpaDistDir(), ipaName+".ipa"));
        FileUtils.forceDelete(ipa);
        //
        List<File> dsyms = CommonUtil.findIosDsymsinDirectory(new File(configurationBinPath));
        if (dsyms.size() == 1 ) {
            File dsymDir = dsyms.get(0);
            ZipUtil.compressDirectory(dsymDir, true, new File(PathUtil.getDsymDistDir(), "dSYM."+env.getName()+".zip"));
            FileUtils.deleteDirectory(dsymDir);
        } else {
            logger.warn("None or multiple DSYMs found! Not moving to distribution folder.");
        }
    }

}
