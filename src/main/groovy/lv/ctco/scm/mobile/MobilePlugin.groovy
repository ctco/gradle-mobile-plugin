/*
 * @(#)MobilePlugin.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile

import lv.ctco.scm.mobile.core.utils.ErrorUtil
import lv.ctco.scm.mobile.platform.android.AndroidPlatform
import lv.ctco.scm.mobile.platform.android.AndroidUtil
import lv.ctco.scm.mobile.platform.common.CommonTasks
import lv.ctco.scm.mobile.platform.xamarin.XamarinConfiguration
import lv.ctco.scm.mobile.platform.xamarin.XamarinExtension
import lv.ctco.scm.mobile.platform.xamarin.XamarinPlatform
import lv.ctco.scm.mobile.platform.xamarin.XamarinUtil
import lv.ctco.scm.mobile.platform.xamarin.XandroidConfiguration
import lv.ctco.scm.mobile.platform.xamarin.XandroidExtension
import lv.ctco.scm.mobile.platform.xcode.XcodeConfiguration
import lv.ctco.scm.mobile.platform.xcode.XcodeExtension
import lv.ctco.scm.mobile.platform.xcode.XcodePlatform
import lv.ctco.scm.mobile.platform.xcode.XcodeUtil

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion

class MobilePlugin implements Plugin<Project> {

    final static Logger logger = Logging.getLogger(MobilePlugin.class)

    @Override
    void apply(Project project) {
        try {
            if (project.state.getFailure() == null) {
                String plugin = MobilePluginUtil.getPluginInfo(
                        this.getClass().getClassLoader().getResourceAsStream("META-INF/gradle-plugins/ctco-mobile.properties"))
                logger.info("Applying '"+plugin+"' to '"+project.getName()+"' project")
                checkMinimumGradleVersion()
            }
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

    private static void detectAndConfigurePlatform(Project project) {
        overrideExtensionProperties(project)
        if (project.ctcoMobile.platform == null) {
            logger.info("Project mobile platform is not defined, performing detection...")
            int detectedPlatforms = 0
            if (AndroidUtil.isAndroidPluginApplied(project)) {
                logger.info("Android platform detected")
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
                logger.warn("Unable to detect a single supported mobile platform, configuring only common tasks")
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

    private static void configureCommonTasks(Project project) {
        CommonTasks.getOrCreateCreateTagTask(project)
        CommonTasks.getOrCreateIpaReprofilingTask(project)
        CommonTasks.getOrCreateKnappsackUploadTask(project)
        CommonTasks.getOrCreateReportGitCommitInfoTask(project)
        project.getPlugins().apply(PublishingTaskRules.class)
    }

    private static void overrideExtensionProperties(Project project) {
        if (project.hasProperty("ctcoMobile.xcode.libraryVersion")) {
            project.ctcoMobile.xcode.libraryVersion = project["ctcoMobile.xcode.libraryVersion"]
        }
        if (project.hasProperty("ctcoMobile.xandroid.androidVersionCode")) {
            project.ctcoMobile.xandroid.androidVersionCode = project["ctcoMobile.xandroid.androidVersionCode"]
        }
    }

    private static void checkMinimumGradleVersion() throws IOException {
        GradleVersion gradleVersionMinimum = GradleVersion.version("2.14.1")
        GradleVersion gradleVersionCurrent = GradleVersion.current()
        if (gradleVersionCurrent < gradleVersionMinimum) {
            throw new IOException("Execution on older Gradle version ("+gradleVersionCurrent.getVersion()+
                    ") than is defined as the minimum ("+gradleVersionMinimum.getVersion()+")")
        }
    }

}
