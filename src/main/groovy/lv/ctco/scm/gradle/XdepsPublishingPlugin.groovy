package lv.ctco.scm.gradle

import lv.ctco.scm.gradle.utils.ErrorUtil
import lv.ctco.scm.gradle.xdeps.XdepsConfiguration
import lv.ctco.scm.gradle.xdeps.XdepsExtension
import lv.ctco.scm.gradle.xdeps.XdepsTasks
import lv.ctco.scm.gradle.xdeps.XdepsUtil

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class XdepsPublishingPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(XdepsPublishingPlugin.class)

    private static final String pluginName = "lv.ctco.scm.xdeps-publishing";

    @Override
    void apply(Project project) {
        try {
            MobilePluginUtil.announcePlugin(pluginName, project.getName())
            MobilePluginUtil.checkMinimumGradleVersion()

            project.extensions.create("xdeps", XdepsExtension)

            project.afterEvaluate {
                overrideExtensionProperties(project)
                XdepsConfiguration xdepsConfiguration = project.xdeps.getXdepsConfiguration()
                if (XdepsUtil.isValidXdepsConfiguration(xdepsConfiguration) && !XdepsUtil.getMavenPublications(project).isEmpty()) {
                    logger.info("Configuring Xdeps tasks...")
                    XdepsTasks.getOrCreatePublishXdepsTask(project)
                    XdepsUtil.applyMavenPublishPlugin(project)
                    XdepsUtil.enforceRequiredMavenRepository(project, XdepsUtil.getRequiredMavenRepositoryName(xdepsConfiguration.getVersion()))
                    XdepsUtil.enforceRequiredXdepsConfiguration(project, xdepsConfiguration)
                    XdepsUtil.applyXdepsPublishRules(project)
                    XdepsTasks.getOrCreateXdepsInfoTask(project, xdepsConfiguration)
                } else {
                    throw new IOException("Valid Xdeps configuration was not found")
                }
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask("applyPlugin", e)
        }
    }

    private static void overrideExtensionProperties(Project project) {
        if (project.hasProperty("xdeps.version")) {
            project.xdeps.version = project["xdeps.version"]
        }
    }

}
