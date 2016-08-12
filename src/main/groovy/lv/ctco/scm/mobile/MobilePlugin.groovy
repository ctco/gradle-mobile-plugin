/*
 * @(#)MobilePlugin.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.platform.android.AndroidPlatform;
import lv.ctco.scm.mobile.platform.android.AndroidUtil;
import lv.ctco.scm.mobile.platform.common.CommonTasks;
import lv.ctco.scm.mobile.platform.xamarin.XamarinExtension;
import lv.ctco.scm.mobile.platform.xamarin.XamarinPlatform;
import lv.ctco.scm.mobile.platform.xamarin.XamarinUtil;
import lv.ctco.scm.mobile.platform.xamarin.XandroidExtension;
import lv.ctco.scm.mobile.platform.xcode.XcodeExtension;
import lv.ctco.scm.mobile.platform.xcode.XcodePlatform;
import lv.ctco.scm.mobile.platform.xcode.XcodeUtil;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.util.GradleVersion

class MobilePlugin implements Plugin<Project> {

    private static final String GRADLE_VERSION_MINIMUM_REQUIRED = "2.14.1";

    @Override
    void apply(Project project) {
        try {
            LoggerUtil.info("Applying C.T.Co Mobile Plugin to '"+project.getName()+"' project");

            if (project.state.getFailure() == null) {
                checkGradleVersion();
            }

            createExtensions(project);
            if (project.state.executed) {
                detectAndConfigurePlatform(project);
            } else {
                project.afterEvaluate {
                    detectAndConfigurePlatform(project);
                }
            }

            LoggerUtil.info("C.T.Co Mobile Plugin has been applied to '"+project.getName()+"' project");
        } catch (IOException e) {
            LoggerUtil.errorInTask("applyPlugin", e.getMessage())
            throw new GradleException(e.getMessage(), e);
        }
    }

    private static void createExtensions(Project project) {
        project.extensions.create("ctcoMobile", MobileExtension);
        project.ctcoMobile.extensions.create("xcode", XcodeExtension);
        project.ctcoMobile.extensions.create("xamarin", XamarinExtension);
        project.ctcoMobile.extensions.create("xandroid", XandroidExtension);
    }

    private static void detectAndConfigurePlatform(Project project) {
        overrideExtensionProperties(project);
        if (project.ctcoMobile.platform == null) {
            LoggerUtil.info("Project mobile platform assemblyType is not defined, performing automatic configuration");
            int detectedPlatforms = 0;
            if (AndroidUtil.isAndroidPluginApplied(project)) {
                LoggerUtil.info("Android platform detected");
                project.ctcoMobile.platform = "android";
                detectedPlatforms++;
            }
            if (XamarinUtil.getSlnCount(project.projectDir) > 0) {
                LoggerUtil.info("Xamarin platform detected");
                project.ctcoMobile.platform = "xamarin";
                detectedPlatforms++;
            }
            if (XcodeUtil.getXcodeprojCount(project.projectDir) > 0) {
                LoggerUtil.info("Xcode platform detected");
                project.ctcoMobile.platform = "xcode";
                detectedPlatforms++;
            }
            if (detectedPlatforms > 1) {
                project.ctcoMobile.platform = null;
            }
        }
        switch (project.ctcoMobile.platform) {
            case "android":
                configureAndroidPlatform(project);
                break;
            case "xamarin":
                configureXamarinPlatform(project);
                break;
            case "xcode":
                configureXcodePlatform(project);
                break;
            default:
                LoggerUtil.warn("Unable to detect a single supported mobile platform, configuring only common tasks")
        }
        configureCommonTasks(project);
    }

    private static void configureAndroidPlatform(Project project) {
        AndroidPlatform platform = new AndroidPlatform();
        platform.setReleaseVersion(project.android.defaultConfig.versionName);
        platform.configure(project);
    }

    private static void configureXamarinPlatform(Project project) {
        XamarinPlatform platform = new XamarinPlatform(project);
        platform.configure(project.ctcoMobile.xamarin, project.ctcoMobile.xandroid);
    }

    private static void configureXcodePlatform(Project project) {
        XcodePlatform platform = new XcodePlatform(project);
        platform.configure(project.ctcoMobile.xcode);
    }

    private static void configureCommonTasks(Project project) {
        CommonTasks.getOrCreateCreateTagTask(project);
        CommonTasks.getOrCreateIpaReprofilingTask(project);
        CommonTasks.getOrCreateKnappsackUploadTask(project);
        project.getPlugins().apply(PublishingTaskRules.class);
    }

    private static void overrideExtensionProperties(Project project) {
        if (project.hasProperty("ctcoMobile.xcode.libraryVersion")) {
            project.ctcoMobile.xcode.libraryVersion = project["ctcoMobile.xcode.libraryVersion"];
        }
        if (project.hasProperty("ctcoMobile.xandroid.androidVersionCode")) {
            project.ctcoMobile.xandroid.androidVersionCode = project["ctcoMobile.xandroid.androidVersionCode"];
        }
    }

    private static void checkGradleVersion() throws IOException {
        GradleVersion gradleVersionMinimum = GradleVersion.version(GRADLE_VERSION_MINIMUM_REQUIRED);
        GradleVersion gradleVersionCurrent = GradleVersion.current();
        LoggerUtil.info("Minimum required Gradle version is defined as " + GRADLE_VERSION_MINIMUM_REQUIRED);
        LoggerUtil.info("Currently used Gradle version is detected as " + gradleVersionCurrent.getVersion());
        if (gradleVersionCurrent < gradleVersionMinimum) {
            throw new IOException("Execution on older Gradle version than is defined as the minimum");
        }
    }

}
