/*
 * @(#)AndroidPlatform.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import lv.ctco.scm.mobile.core.objects.TaskGroup;
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;

public final class AndroidPlatform {

    private String releaseVersion;
    private String revision;

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void configure(Project project) throws IOException {
        revision = RevisionUtil.getRevision(project);
        GitUtil.generateCommitInfo(project);

        AndroidTasks.getOrCreateProjectInfoTask(project, releaseVersion, revision);

        project.getTasks().create("runUnitTests", DefaultTask.class);

        Task build = project.getTasks().create("build", DefaultTask.class);
        build.setGroup(TaskGroup.BUILD.getLabel());
        Task buildAndroid = project.getTasks().create("buildAndroid", DefaultTask.class);

        Task assembleDebug = AndroidTasks.getTaskByName(project, "assembleDebug");
        Task assembleRelease = AndroidTasks.getTaskByName(project, "assembleRelease");

        Task buildDebug = project.getTasks().create("buildAndroidDebug", DefaultTask.class);
        Environment debug = generateEnvironmentForBuildDebug(project);
        addUtilityTasksForBuildDebug(project, debug);

        Task buildRelease = project.getTasks().create("buildAndroidRelease", DefaultTask.class);
        Environment release = generateEnvironmentForBuildRelease(project);
        addUtilityTasksForBuildRelease(project, release);

        buildDebug.dependsOn(assembleDebug);
        buildRelease.dependsOn(assembleRelease);
        buildAndroid.dependsOn(buildRelease);
        build.dependsOn(buildAndroid);
    }

    private Environment generateEnvironmentForBuildRelease(Project project) {
        Environment environment = new Environment();
        environment.setName("release");
        environment.setConfiguration("release");
        environment.setBuildVersion(releaseVersion+"."+revision);
        environment.setAssemblyName(project.getName());
        if (AndroidUtil.isAndroidAppPluginApplied(project)) {
            environment.setAssemblyType("apk");
            environment.setManifestPath(new File(project.getProjectDir(),
                    "build/intermediates/manifests/full/release/"));
            environment.setAssemblyPath(new File(project.getProjectDir(),
                    "build/outputs/apk/"));
        }
        if (AndroidUtil.isAndroidLibraryPluginApplied(project)) {
            environment.setAssemblyType("aar");
            environment.setManifestPath(new File(project.getProjectDir(),
                    "build/intermediates/bundles/release/"));
            environment.setAssemblyPath(new File(project.getProjectDir(),
                    "build/outputs/aar/"));
        }
        return environment;
    }

    private void addUtilityTasksForBuildRelease(Project project, Environment environment) {
        AndroidTasks.getTaskByName(project, "processReleaseManifest").finalizedBy(
                AndroidTasks.getOrCreateUpdateVersionAndroidTask(project, environment)
        );
        AndroidTasks.getTaskByName(project, "assembleRelease").finalizedBy(
                AndroidTasks.getOrCreateArchiveArtifactTask(project, environment)
        );
    }

    private Environment generateEnvironmentForBuildDebug(Project project) {
        Environment environment = new Environment();
        environment.setName("debug");
        environment.setConfiguration("debug");
        environment.setBuildVersion(releaseVersion+"."+revision);
        environment.setAssemblyName(project.getName());
        if (AndroidUtil.isAndroidAppPluginApplied(project)) {
            environment.setAssemblyType("apk");
            environment.setManifestPath(new File(project.getProjectDir(),
                    "build/intermediates/manifests/full/debug/"));
            environment.setAssemblyPath(new File(project.getProjectDir(),
                    "build/outputs/apk/"));
        }
        if (AndroidUtil.isAndroidLibraryPluginApplied(project)) {
            environment.setAssemblyType("aar");
            environment.setManifestPath(new File(project.getProjectDir(),
                    "build/intermediates/bundles/debug/"));
            environment.setAssemblyPath(new File(project.getProjectDir(),
                    "build/outputs/aar/"));
        }
        return environment;
    }

    private void addUtilityTasksForBuildDebug(Project project, Environment environment) {
        AndroidTasks.getTaskByName(project, "processDebugManifest").finalizedBy(
                AndroidTasks.getOrCreateUpdateVersionAndroidTask(project, environment)
        );
        AndroidTasks.getTaskByName(project, "assembleDebug").finalizedBy(
                AndroidTasks.getOrCreateArchiveArtifactTask(project, environment)
        );
    }

    /*
    private void performDynamicUpdateOfAndroidTaskGraph(Project project) throws IOException {
        for (Object foundObject : project.getTasks().toArray()) {
            Task foundTask = (Task)foundObject;
            String foundTaskName = foundTask.getName();
            if (AndroidUtil.isProcessManifestTask(foundTaskName)) {
                foundTaskName = AndroidUtil.getTaskTarget(foundTaskName);
                foundTask.finalizedBy(AndroidTasks.getOrCreateUpdateVersionAndroidTask(project, foundTaskName, releaseVersion, revision, new File(".")));
            } else if (AndroidUtil.isAssembleTask(foundTaskName)) {
                foundTaskName = AndroidUtil.getTaskTarget(foundTaskName);
                foundTask.finalizedBy(AndroidTasks.getOrCreateUpdateVersionAndroidTask(project, foundTaskName, releaseVersion, revision, new File(".")));
            }
        }
    }
    */

}
