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
            MobilePluginUtil.checkMinimumGradleVersion()
            project.getExtensions().create("xdeps", XdepsExtension)
            XdepsUtil.applyMavenPublishPlugin(project)
            project.afterEvaluate {
                checkXdepsVersionOverride(project)
                XdepsExtension xdepsExtension = (XdepsExtension) project.getExtensions().getByName("xdeps")
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

    private static void checkXdepsVersionOverride(Project project) {
        if (project.hasProperty("xdeps.version")) {
            project.xdeps.version = project["xdeps.version"]
        }
    }

}
