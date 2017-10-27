package lv.ctco.scm.gradle

import lv.ctco.scm.gradle.utils.ErrorUtil
import lv.ctco.scm.gradle.xdeps.XdepsConfiguration
import lv.ctco.scm.gradle.xdeps.XdepsExtension
import lv.ctco.scm.gradle.xdeps.XdepsTasks
import lv.ctco.scm.gradle.xdeps.XdepsUtil

import org.gradle.api.Plugin
import org.gradle.api.Project

public class XdepsPublishingPlugin implements Plugin<Project> {

    private static final String pluginName = "lv.ctco.scm.xdeps-publishing";

    @Override
    void apply(Project project) {
        try {
            MobilePluginUtil.announcePlugin(pluginName, project.getName())
            MobilePluginUtil.checkMinimumGradleVersion()
            project.extensions.create("xdeps", XdepsExtension)
            project.afterEvaluate {
                checkXdepsVersionOverride(project)
                XdepsConfiguration xdepsConfiguration = project.xdeps.getXdepsConfiguration()
                if (XdepsUtil.isValidXdepsConfiguration(project, xdepsConfiguration)) {
                    XdepsUtil.applyMavenPublishPlugin(project)
                    XdepsTasks.getOrCreatePublishXdepsSnapshotsTask(project)
                    XdepsTasks.getOrCreatePublishXdepsReleasesTask(project)
                    XdepsUtil.applyXdepsPublishRules(project)
                    XdepsUtil.enforceRequiredXdepsConfiguration(project, xdepsConfiguration)
                    XdepsTasks.getOrCreateXdepsInfoTask(project, xdepsConfiguration)
                } else {
                    throw new IOException("Missing configuration for Xdeps publishing")
                }
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask("applyPlugin", e)
        }
    }

    private static void checkXdepsVersionOverride(Project project) {
        if (project.hasProperty("xdeps.version")) {
            project.xdeps.version = project["xdeps.version"]
        }
    }

}
