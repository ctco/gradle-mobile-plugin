/*
 * @(#)XdepsPublishingPlugin.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle

import lv.ctco.scm.gradle.utils.ErrorUtil
import lv.ctco.scm.gradle.xdeps.XdepsConfiguration
import lv.ctco.scm.gradle.xdeps.XdepsExtension
import lv.ctco.scm.gradle.xdeps.XdepsTasks
import lv.ctco.scm.gradle.xdeps.XdepsUtil
import lv.ctco.scm.mobile.utils.VersionUtil

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException

public class XdepsPublishingPlugin implements Plugin<Project> {

    private static final String pluginName = "lv.ctco.scm.xdeps-publishing";

    @Override
    void apply(Project project) {
        // Can and must be applied only to root project because of maven-publish plugin's peculiarities
        if (!project.equals(project.getRootProject())) {
            MobilePluginUtil.announcePluginIgnore(pluginName, project.getName())
            return
        }
        try {
            MobilePluginUtil.announcePluginApply(pluginName, project.getName())
            MobilePluginUtil.createXdepsDependencyConfiguration(project)
            // As this must be the root project - create xdeps for all child projects
            for (Project child : project.getAllprojects()) {
                MobilePluginUtil.createXdepsDependencyConfiguration(child)
            }
            project.getExtensions().create("xdeps", XdepsExtension)
            XdepsUtil.applyMavenPublishPlugin(project)
            project.afterEvaluate {
                checkXdepsVersion(project)
                checkXdepsVersionOverride(project)
                XdepsExtension xdepsExtension = (XdepsExtension)project.getExtensions().getByName("xdeps")
                XdepsConfiguration xdepsConfiguration = xdepsExtension.getXdepsConfiguration()
                XdepsUtil.checkXdepsConfiguration(xdepsConfiguration)
                XdepsTasks.getOrCreateXdepsPublishSnapshotsTask(project)
                XdepsTasks.getOrCreateXdepsPublishReleasesTask(project)
                XdepsTasks.getOrCreateXdepsDisplayInfoTask(project, xdepsConfiguration)
                XdepsUtil.applyXdepsPublishRules(project)
            }
        } catch (IOException|UnknownDomainObjectException e) {
            ErrorUtil.errorInTask("applyPlugin", e)
        }
    }

    private static void checkXdepsVersion(Project project) {
        String version = project.xdeps.version.toString().minus("-SNAPSHOT")
        if (!VersionUtil.isMajorMinorPatchVersion(version)) {
            ErrorUtil.errorInTask(this.getName(), "Xdeps version must be in form of major.minor.patch[-SNAPSHOT]")
        }
    }

    private static void checkXdepsVersionOverride(Project project) {
        if (!project.hasProperty("xdeps.version")) {
            return
        }
        String override = project["xdeps.version"].toString()
        if (VersionUtil.isSnapshotVersion(override)) {
            ErrorUtil.errorInTask(this.getName(), "Xdeps version override must not be a snapshot")
        }
        if (!VersionUtil.isValidVersionString(override)) {
            ErrorUtil.errorInTask(this.getName(), "Xdeps version override must be a valid version string")
        }
        String version = project.xdeps.version.toString().minus("-SNAPSHOT")
        if (!(override.equals(version) || override.startsWith(version+'.'))) {
            ErrorUtil.errorInTask(this.getName(), "Xdeps version override must match existing major.minor.patch base version")
        }
        project.xdeps.version = override
    }

}
