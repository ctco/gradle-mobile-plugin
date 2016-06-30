/*
 * @(#)MobilePlugin.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;
import lv.ctco.scm.mobile.platform.common.CommonTasks;
import lv.ctco.scm.mobile.platform.xamarin.XamarinExtension;
import lv.ctco.scm.mobile.platform.xamarin.XamarinPlatform;
import lv.ctco.scm.mobile.platform.xamarin.XandroidExtension;
import lv.ctco.scm.mobile.platform.xcode.XcodeExtension;
import lv.ctco.scm.mobile.platform.xcode.XcodePlatform;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

class MobilePlugin implements Plugin<Project> {

    private static final String MINIMUM_REQUIRED_GRADLE_VERSION = "2.14"

    protected Project project

    /* Allows overriding plugin's extension parameters from commandline execution. */
    protected void readConventionsFromProperties() {
        /* Property to override Xcode library version value and task execution. */
        if (project.hasProperty("ctcoMobile.xcode.libraryVersion")) {
            project.ctcoMobile.xcode.libraryVersion = project["ctcoMobile.xcode.libraryVersion"]
        }
        /* Property to specify Android verson code in artifact's manifest file. */
        if (project.hasProperty("ctcoMobile.xandroid.androidVersionCode")) {
            project.ctcoMobile.xandroid.androidVersionCode = project["ctcoMobile.xandroid.androidVersionCode"]
        }
    }

    @Override
    void apply(Project project) {
        try {
            this.project = project;
            PropertyUtil.setProject(project);
            LoggerUtil.info("Applying C.T.Co Mobile Plugin");

            if (project.state.getFailure() == null) {
                readPluginInfo();
                checkGradleVersion();
            }

            project.extensions.create("ctcoMobile", MobileExtension);
            project.ctcoMobile.extensions.create("xcode", XcodeExtension);
            project.ctcoMobile.extensions.create("xamarin", XamarinExtension);
            project.ctcoMobile.extensions.create("xandroid", XandroidExtension);

            if (project.state.executed) {
                readConventionsFromProperties();
                performDynamicConfiguration();
            } else {
                project.afterEvaluate {
                    readConventionsFromProperties();
                    performDynamicConfiguration();
                }
            }

            LoggerUtil.info("Applying mobile library Maven publishing rules");
            project.getPlugins().apply(MavenPublishingRules.class);
            LoggerUtil.info("C.T.Co Mobile Plugin has been applied");
        } catch (IOException e) {
            LoggerUtil.errorInTask("applyPlugin", e.getMessage())
            throw new GradleException(e.getMessage(), e);
        }
    }

    public void performDynamicConfiguration() {
        if (!project.ctcoMobile.platform) {
            LoggerUtil.info('Project mobile platform type is not defined, performing automatic configuration')
            autoDetectMobilePlatform()
        }
        if (project.ctcoMobile.platform) {
            configurePlatforms()
        } else {
            LoggerUtil.warn('Unable to detect a mobile platform, configuring only non-build tasks')
        }
        setupCommonTasks()
    }

    private void setupCommonTasks() {
        CommonTasks.getOrCreateCreateTagTask(project)
        CommonTasks.getOrCreateIpaReprofilingTask(project)
        CommonTasks.getOrCreateKnappsackUploadTask(project)
        CommonTasks.createTarSourcesTask(project)
    }

    private void autoDetectMobilePlatform() {
        List projectFiles = project.projectDir.listFiles().findAll { it.name.endsWith('.xcodeproj') }
        if (projectFiles.size() == 1) {
            LoggerUtil.info("Xcode platform detected, because ${projectFiles[0]} file" +
                    " is present in the project directory")
            project.ctcoMobile.platform = XcodePlatform.NAME
        } else if (projectFiles.size() > 1) {
            throw new IOException('More that one .xproject file detected,' +
                    ' multiple project builds are not supported')
        }
        List solutionFiles = project.projectDir.listFiles().findAll { it.name.endsWith('.sln') }
        if (solutionFiles.size() == 1) {
            if (project.ctcoMobile.platform != null) {
                throw new IOException('Both Xcode and Xamarin projects detected, unable to choose platform')
            } else {
                LoggerUtil.info("Xamarin platform detected, because ${solutionFiles[0]} file" +
                        " is present in the project directory")
                project.ctcoMobile.platform = XamarinPlatform.NAME
                project.ctcoMobile.xamarin.solutionFile = solutionFiles[0]
            }
        } else if (solutionFiles.size() > 1) {
            throw new IOException('More that one .sln file detected,' +
                    ' multiple project builds are not supported')
        }
    }

    private void configurePlatforms() {
        String platformName = project.ctcoMobile.platform
        if (platformName == XcodePlatform.NAME) {
            List projectFiles = project.projectDir.listFiles().findAll { it.name.endsWith('.xcodeproj') }
            if (projectFiles.size() == 1) {
                XcodePlatform platform = new XcodePlatform(project)
                platform.configure(project.ctcoMobile.xcode)
            } else {
                //LoggerUtil.warn("Xcode platform detected, but no project file")
                //LoggerUtil.warn('Unable to detect a mobile platform, configuring only non-build tasks')
                //Disabled until platform extension configuration fix...
                XcodePlatform platform = new XcodePlatform(project)
                platform.configure(project.ctcoMobile.xcode)
            }
        } else if (platformName == XamarinPlatform.NAME) {
            XamarinPlatform platform = new XamarinPlatform(project)
            platform.configure(project.ctcoMobile.xamarin, project.ctcoMobile.xandroid)
        } else {
            throw new IOException("Build of platform '"+platformName+"' is not supported")
        }
    }

    private void readPluginInfo() {
        InputStream pluginInfoStream = this.getClass().getClassLoader().getResourceAsStream("plugin-info.properties");
        if (pluginInfoStream != null) {
            Properties pluginInfoProperties = new Properties();
            pluginInfoProperties.load(pluginInfoStream);
            PropertyUtil.setPluginGroup(pluginInfoProperties.getProperty("plugin-group"));
            PropertyUtil.setPluginName(pluginInfoProperties.getProperty("plugin-name"));
            PropertyUtil.setPluginVersion(pluginInfoProperties.getProperty("plugin-version"));
            TeamcityUtil.setAgentParameter("tools.build.plugin.group", PropertyUtil.getPluginGroup());
            TeamcityUtil.setAgentParameter("tools.build.plugin.name", PropertyUtil.getPluginName());
            TeamcityUtil.setAgentParameter("tools.build.plugin.version", PropertyUtil.getPluginVersion());
        }
    }

    private static void checkGradleVersion() {
        LoggerUtil.info("Minimum required Gradle version is defined as "+MINIMUM_REQUIRED_GRADLE_VERSION);
        GradleVersion gradleVersionMinimum = GradleVersion.version(MINIMUM_REQUIRED_GRADLE_VERSION);
        GradleVersion gradleVersionCurrent = GradleVersion.current();
        LoggerUtil.info("Currently used Gradle version is detected as "+gradleVersionCurrent.getVersion());
        if (gradleVersionCurrent < gradleVersionMinimum) {
            throw new IOException("Execution on older Gradle version than is defined as the minimum")
        }
    }

}
