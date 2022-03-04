package lv.ctco.scm.gradle.xdeps;

import lv.ctco.scm.utils.file.FileUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class XdepsCreateXdepsMavenRepositoryTask extends DefaultTask {

    private List<String> modules;
    private File repositoryDir;

    @Input
    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    @OutputDirectory
    public File getRepositoryDir() {
        return repositoryDir;
    }

    public void setRepositoryDir(File repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    @TaskAction
    public void doTaskAction() {
        if (repositoryDir.exists()) { // TODO : what if symlink ?
            try {
                FileUtil.cleanDirectory(repositoryDir);
            } catch (IOException e) {
                throw new TaskExecutionException(this, e);
            }
        }

        Configuration xdepsConfiguration = getProject().getConfigurations().getByName(XdepsPlugin.XDEPS_CONFIGURATION_NAME);
        List<ModuleComponentIdentifier> xdepsSelectedModuleComponentIdentifiers = new ArrayList<>();

        Set<? extends DependencyResult> xdepsAllDependencies = xdepsConfiguration.getIncoming().getResolutionResult().getAllDependencies();
        for (DependencyResult xdepsDependency : xdepsAllDependencies) {
            ModuleComponentIdentifier moduleComponentIdentifier = (ModuleComponentIdentifier) ((ResolvedDependencyResult) xdepsDependency).getSelected().getId();
            for (String module : modules) {
                if (moduleComponentIdentifier.getModule().equals(module)) {
                    xdepsSelectedModuleComponentIdentifiers.add(moduleComponentIdentifier);
                }
            }
        }

        Set<ComponentArtifactsResult> componentArtifactsResults = getProject().getDependencies()
                .createArtifactResolutionQuery().forComponents(xdepsSelectedModuleComponentIdentifiers)
                .withArtifacts(MavenModule.class, MavenPomArtifact.class).execute().getResolvedComponents();
        for (ComponentArtifactsResult pomComponent : componentArtifactsResults) {
            for (ArtifactResult artifactResult : pomComponent.getArtifacts(MavenPomArtifact.class)) {
                if (artifactResult instanceof ResolvedArtifactResult) {
                    ResolvedArtifactResult resolvedArtifactResult = (ResolvedArtifactResult) artifactResult;
                    ModuleComponentIdentifier moduleComponentIdentifier = (ModuleComponentIdentifier) resolvedArtifactResult.getId().getComponentIdentifier();
                    File repositoryModuleDir = new File(repositoryDir, moduleComponentIdentifier.getGroup().replace('.','/')+"/"+moduleComponentIdentifier.getModule()+"/"+moduleComponentIdentifier.getVersion());
                    try {
                        Files.createDirectories(repositoryModuleDir.toPath());
                        Files.copy(resolvedArtifactResult.getFile().toPath(), new File(repositoryModuleDir, resolvedArtifactResult.getFile().getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        for (File xdepsConfigurationFile : xdepsConfiguration.getIncoming().getFiles()) {
                            if (xdepsConfigurationFile.getName().startsWith(moduleComponentIdentifier.getModule()+"-"+moduleComponentIdentifier.getVersion())) {
                                Files.copy(xdepsConfigurationFile.toPath(), new File(repositoryModuleDir, xdepsConfigurationFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } catch (IOException e) {
                        throw new TaskExecutionException(this, e);
                    }
                }
            }
        }
    }

}
