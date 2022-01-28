package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.mobile.utils.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class XdepsRestoreDependenciesTask extends DefaultTask {

    private Set<File> xdepsFiles;
    private File outputDirectory;

    public XdepsRestoreDependenciesTask() {
        this.setGroup(XdepsPlugin.XDEPS_TASK_GROUP);
        this.setDescription("Set up files from the xdeps configuration");
    }

    @InputFiles
    public Set<File> getXdepsFiles() {
        return xdepsFiles;
    }

    public void setXdepsFiles(Set<File> xdepsFiles) {
        this.xdepsFiles = xdepsFiles;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            FileUtils.forceMkdir(outputDirectory);
            FileUtils.cleanDirectory(outputDirectory);
        } catch (IOException e) {
            throw new TaskExecutionException(this, e);
        }
        for (File xdepsFile : xdepsFiles) {
            try {
                if (FilenameUtils.isExtension(xdepsFile.getName(), "zip")) {
                    ZipUtil.extractAll(xdepsFile, outputDirectory);
                } else {
                    FileUtils.copyFileToDirectory(xdepsFile, outputDirectory);
                }
            } catch (IOException e) {
                throw new TaskExecutionException(this, e);
            }
        }
    }

}
