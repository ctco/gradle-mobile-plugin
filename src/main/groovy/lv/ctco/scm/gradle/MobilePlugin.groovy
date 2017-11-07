/*
 * @(#)MobilePlugin.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle

import lv.ctco.scm.gradle.android.AndroidPlatform
import lv.ctco.scm.gradle.common.CommonTasks
import lv.ctco.scm.gradle.utils.ErrorUtil
import lv.ctco.scm.gradle.xamarin.XamarinConfiguration
import lv.ctco.scm.gradle.xamarin.XamarinExtension
import lv.ctco.scm.gradle.xamarin.XamarinPlatform
import lv.ctco.scm.gradle.xamarin.XamarinUtil
import lv.ctco.scm.gradle.xamarin.XandroidConfiguration
import lv.ctco.scm.gradle.xamarin.XandroidExtension
import lv.ctco.scm.gradle.xcode.XcodeConfiguration
import lv.ctco.scm.gradle.xcode.XcodeExtension
import lv.ctco.scm.gradle.xcode.XcodePlatform
import lv.ctco.scm.gradle.xcode.XcodeUtil

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class MobilePlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(MobilePlugin.class)

    @Override
    void apply(Project project) {
        try {
            if (project.state.getFailure() == null) {
                MobilePluginUtil.announcePluginApply(MobilePluginUtil.getPluginName(), project.getName())
                MobilePluginUtil.checkMinimumGradleVersion()
            }
            MobilePluginUtil.createXdepsDependencyConfiguration(project)
            createExtensions(project)
            if (project.state.executed) {
                detectAndConfigurePlatform(project)
            } else {
                project.afterEvaluate {
                    detectAndConfigurePlatform(project)
                }
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask("applyPlugin", e)
        }
    }

    private static void createExtensions(Project project) {
        project.extensions.create("ctcoMobile", MobileExtension)
        project.ctcoMobile.extensions.create("xcode", XcodeExtension)
        project.ctcoMobile.extensions.create("xamarin", XamarinExtension)
        project.ctcoMobile.extensions.create("xandroid", XandroidExtension)
    }

    private static void detectAndConfigurePlatform(Project project) throws IOException {
        overrideExtensionProperties(project)
        if (project.ctcoMobile.platform == null) {
            logger.info("Project mobile platform is not defined, performing detection...")
            int detectedPlatforms = 0
            if (MobilePluginUtil.isAndroidAppPluginApplied(project)) {
                logger.info("Android application detected")
                project.ctcoMobile.platform = "android"
                detectedPlatforms++
            }
            if (MobilePluginUtil.isAndroidLibraryPluginApplied(project)) {
                logger.info("Android library detected")
                project.ctcoMobile.platform = "android"
                detectedPlatforms++
            }
            if (XamarinUtil.getSlnCount(project.projectDir) > 0) {
                logger.info("Xamarin platform detected")
                project.ctcoMobile.platform = "xamarin"
                detectedPlatforms++
            }
            if (XcodeUtil.getXcodeprojCount(project.projectDir) > 0) {
                logger.info("Xcode platform detected")
                project.ctcoMobile.platform = "xcode"
                detectedPlatforms++
            }
            if (detectedPlatforms > 1) {
                project.ctcoMobile.platform = null
            }
        }
        switch (project.ctcoMobile.platform) {
            case "android":
                configureAndroidPlatform(project)
                break
            case "xamarin":
                configureXamarinPlatform(project)
                break
            case "xcode":
                configureXcodePlatform(project)
                break
            default:
                logger.warn("Unable to detect a single supported mobile platform")
                logger.info("Configuring only common tasks...")
        }
        configureCommonTasks(project)
    }

    private static void configureAndroidPlatform(Project project) {
        AndroidPlatform platform = new AndroidPlatform()
        platform.setReleaseVersion(project.android.defaultConfig.versionName)
        platform.configure(project)
    }

    private static void configureXamarinPlatform(Project project) {
        XamarinPlatform platform = new XamarinPlatform(project)
        XamarinConfiguration xamarinConfiguration = project.ctcoMobile.xamarin.getXamarinConfiguration()
        XandroidConfiguration xandroidConfiguration = project.ctcoMobile.xandroid.getXandroidConfiguration()
        platform.configure(xamarinConfiguration, xandroidConfiguration)
    }

    private static void configureXcodePlatform(Project project) {
        XcodePlatform platform = new XcodePlatform()
        XcodeConfiguration configuration = project.ctcoMobile.xcode.getXcodeConfiguration()
        platform.configure(project, configuration)
    }

    private static void configureCommonTasks(Project project) throws IOException {
        CommonTasks.getOrCreateIpaReprofilingTask(project)
        CommonTasks.getOrCreateKnappsackUploadTask(project)
        CommonTasks.getOrCreateReportGitCommitInfoTask(project)
    }

    private static void overrideExtensionProperties(Project project) {
        if (project.hasProperty("ctcoMobile.xandroid.androidVersionCode")) {
            project.ctcoMobile.xandroid.androidVersionCode = project["ctcoMobile.xandroid.androidVersionCode"]
        }
    }

}
