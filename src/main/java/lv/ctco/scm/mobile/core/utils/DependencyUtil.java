/*
 * @(#)DependencyUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public final class DependencyUtil {

    private static final Logger logger = Logging.getLogger(DependencyUtil.class);

    private DependencyUtil() {}

    public static void restoreXdepsDependencies(Project project) throws IOException {
        Configuration xdepsConf = project.getConfigurations().findByName("xdeps");
        if (xdepsConf == null) {
            logger.info("No dependency configuration 'xdeps' found");
        } else {
            DependencySet xdepsSet = xdepsConf.getAllDependencies();
            logger.info("Detected {} dependencies for 'xdeps' configuration", xdepsSet.size());
            for (Dependency xdep : xdepsSet) {
                logger.info("    "+xdep.getGroup()+":"+xdep.getName()+":"+xdep.getVersion());
            }

            logger.info("Clearing out \'Libraries\' directory...");
            File librariesDir;
            try {
                librariesDir = PathUtil.getLibrariesDir();
                FileUtils.cleanDirectory(librariesDir);
            } catch (IOException e) {
                throw new IOException("IOException while restoring dependencies!", e);
            }

            Set<File> xdepsFiles = xdepsConf.getFiles();
            logger.info("Detected {} file(s) for 'xdeps' configuration", xdepsFiles.size());
            for (File xdepsFile : xdepsFiles) {
                if (xdepsFile.getName().toLowerCase().endsWith(".zip")) {
                    logger.info("Extracting "+xdepsFile.getName());
                    ZipUtil.extractAll(xdepsFile, librariesDir);
                } else {
                    logger.info("Copying "+xdepsFile.getName());
                    FileUtils.copyFileToDirectory(xdepsFile, librariesDir);
                }
            }
            logger.info("'xdeps' dependency restoration done");
        }
    }

    public static void restoreNugetDependencies(File solutionFile, File nugetPackagesConfigRootDir) throws IOException {
        if (nugetPackagesConfigRootDir != null) {
            logger.info("Using packages.config files in path '"+nugetPackagesConfigRootDir.getAbsolutePath()+"' to restore dependencies");
            String[] extensions = new String[]{"config"};
            File rootCacheDir = NugetUtil.getCacheDirPath(new File(nugetPackagesConfigRootDir, "/."));
            Collection<File> files = FileUtils.listFiles(nugetPackagesConfigRootDir, extensions, true);
            for (File pcFile : files) {
                if ("packages.config".equals(pcFile.getName())) {
                    File cacheDir = NugetUtil.hasCacheDirConfig(pcFile) ? NugetUtil.getCacheDirPath(pcFile) : rootCacheDir;
                    checkNugetRestoration(execNugetRestoration(pcFile, cacheDir));
                }
            }
        } else {
            logger.info("Using solution file read from extension '"+solutionFile.getAbsolutePath()+"' to restore dependencies");
            File cacheDir = NugetUtil.getCacheDirPath(solutionFile);
            checkNugetRestoration(execNugetRestoration(solutionFile, cacheDir));
        }
    }

    private static void checkNugetRestoration(ExecResult execResult) throws IOException {
        if (!execResult.isSuccess()) {
            throw new IOException(execResult.getException());
        }
    }

    private static ExecResult execNugetRestoration(File projectFile, File cacheDir) {
        CommandLine commandLine = new CommandLine("nuget");
        String[] commandArgs = new String[]{"restore", projectFile.getAbsolutePath(), "-o", cacheDir.getAbsolutePath()};
        commandLine.addArguments(commandArgs, false);
        return ExecUtil.execCommand(commandLine, null, null, true, true);
    }

}
