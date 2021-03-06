/*
 * @(#)XcodePlatformTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode

import lv.ctco.scm.mobile.utils.RevisionUtil

import org.junit.Before

class XcodePlatformTest {

    @Before
    public void setUp() throws Exception {
        RevisionUtil.setRevision("0")
        XcodeUtil.setXcodeproj(new File("dummy"))
    }

    /*

    @Test
    void autodetectMultiTarget() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        XcodeUtil.setTargets(['PRJ DEV', 'PRJ TRAIN', 'PRJ UAT'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertEquals extension.environments.size(), 3
        assertTrue extension.environments.containsKey('DEV')
        assertTrue extension.environments.containsKey('TRAIN')
        assertTrue extension.environments.containsKey('UAT')
        assertEquals extension.environments['DEV'].target, 'PRJ DEV'
        assertEquals extension.environments['TRAIN'].target, 'PRJ TRAIN'
        assertEquals extension.environments['UAT'].target, 'PRJ UAT'
    }

    @Test
    void autodetectSingleTarget() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        XcodeUtil.setTargets(['PRJ', 'PRJTest'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertEquals extension.environments.size(), 1
        assertTrue extension.environments.containsKey('DEFAULT')
        assertEquals extension.environments['DEFAULT'].target, 'PRJ'
    }

    @Test
    void autodetectSingleTargetLooksLikeMultiTarget() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        XcodeUtil.setTargets(['PRJ DEV', 'PRJTest'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertEquals extension.environments.size(), 1
        assertTrue extension.environments.containsKey('DEFAULT')
        assertEquals extension.environments['DEFAULT'].target, 'PRJ DEV'
    }

    @Test
    void autodetectMultiTargetConflictsWithManuallyConfigured() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        extension.environment name: 'DEV', target: 'PRJ DEV CUSTOM'
        extension.environment name: 'TrAIN', target: 'PRJ TRAIN CUSTOM'
        XcodeUtil.setTargets(['PRJ DEV', 'PRJ TRAIN', 'PRJ UAT'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertTrue extension.environments.containsKey('DEV')
        assertTrue extension.environments.containsKey('TrAIN')
        assertTrue extension.environments.containsKey('UAT')
        assertEquals extension.environments['DEV'].target, 'PRJ DEV CUSTOM'
        assertEquals extension.environments['TrAIN'].target, 'PRJ TRAIN CUSTOM'
        assertEquals extension.environments['UAT'].target, 'PRJ UAT'
        assertEquals extension.environments.size(), 3
    }

    @Test
    void autodetectSingleTargetConflictsWithManuallyConfigured() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        extension.environment name: 'Default', target: 'PRJ DEV CUSTOM'
        XcodeUtil.setTargets(['PRJ', 'PRJTest'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertTrue extension.environments.containsKey('Default')
        assertEquals extension.environments['Default'].target, 'PRJ DEV CUSTOM'
        assertEquals extension.environments.size(), 1
    }

    @Test
    void autodetectSingleTargetThereIsEnvironmentWithOtherTarget() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        extension.environment name: 'DEV', target: 'PRJ DEV CUSTOM'
        XcodeUtil.setTargets(['PRJ', 'PRJTest'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertTrue extension.environments.containsKey('DEFAULT')
        assertTrue extension.environments.containsKey('DEV')
        assertEquals extension.environments['DEFAULT'].target, 'PRJ'
        assertEquals extension.environments['DEV'].target, 'PRJ DEV CUSTOM'
        assertEquals extension.environments.size(), 2
    }

    @Test
    void autodetectSingleTargetThereIsEnvironmentWithSameTarget() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        extension.environment name: 'DEV', target: 'PRJ DEV CUSTOM'
        XcodeUtil.setTargets(['PRJ DEV CUSTOM', 'PRJTest'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertTrue extension.environments.containsKey('DEV')
        assertEquals extension.environments['DEV'].target, 'PRJ DEV CUSTOM'
        assertEquals extension.environments.size(), 1
    }

    // See https://confluence.ctco.lv/confluence/x/3j9tAg confluence page for a description.
    @Test
    void testCaseFromDocumentation() {
        def project = ProjectBuilder.builder().build()
        XcodeExtension extension = new XcodeExtension()
        extension.environment name: 'PROD', target: 'PRODUCTION'
        extension.environment name: 'DEMO', target: 'iDoStuff DemoClientA'
        XcodeUtil.setTargets(['iDoStuff DEV', 'iDoStuff TRAIN', 'PRODUCTION', 'iDoStuffTests',
                                        'iDoStuff DemoClientA'])
        XcodePlatform platform = new XcodePlatform(project)
        platform.performAutomaticEnvironmentConfiguration(extension)
        //
        assertTrue extension.environments.containsKey('DEV')
        assertTrue extension.environments.containsKey('TRAIN')
        assertTrue extension.environments.containsKey('PROD')
        assertTrue extension.environments.containsKey('DEMO')
        assertEquals extension.environments['DEV'].target, 'iDoStuff DEV'
        assertEquals extension.environments['TRAIN'].target, 'iDoStuff TRAIN'
        assertEquals extension.environments['PROD'].target, 'PRODUCTION'
        assertEquals extension.environments['DEMO'].target, 'iDoStuff DemoClientA'
        assertEquals extension.environments.size(), 4

    }

    @Test
    void containsTaskBundleVersionUpdate() {
        Project project = getProjectConfigure()
        //
        assert project.tasks.findByName('updateVersionDev') instanceof DefaultTask
    }

    @Test
    void xcodeBuildTaskBundleVersionUpdate() {
        Project project = getProjectConfigure()

        def fooTask = project.tasks.findByName('buildDev')
        assertTrue fooTask != null

        Task listTask = project.tasks.findByName('buildDev').dependsOn.find {
            it instanceof DefaultTask && it.name == 'updateVersionDev'
        }
        assertTrue listTask != null
    }

    @Test
    void xcodeBuildTaskDependsOnRestoreDependencies() {
        Project project = getProjectConfigure()

        def fooTask = project.tasks.findByName('buildDev')
        assertTrue fooTask != null

        Task listTask = project.tasks.findByName('buildDev').dependsOn.find {
            it instanceof DefaultTask && it.name == 'restoreDependencies'
        }
        assertTrue listTask != null

    }

    @Test
    void xcodeUITestTasksWithoutUIAutomationSetup() {
        Project project = getProjectConfigure()
        def buildTask = project.tasks.findByName('buildUITestApp')
        assertTrue buildTask == null
    }

    @Test
    void xcodeUITestTasksWithUIAutomationSetup() {
        Project project = getProjectConfigureWithUIAutomationSetup('iGREW_UIT', 'iGREW', null, null, ['testA.js', 'testB.js'] as String[])

        def buildTask = project.tasks.findByName('buildUITestApp')
        assertTrue buildTask != null

        def testAllTask = project.tasks.findByName('runUITests')
        assertTrue testAllTask != null

        def test1Task = project.tasks.findByName('runUITest01testA')
        assertTrue test1Task != null

        def test2Task = project.tasks.findByName('runUITest02testB')
        assertTrue test2Task != null

        Task dep1Task = project.tasks.findByName('runUITest01testA').dependsOn.find {
            it instanceof DefaultTask && it.name == 'buildUITestApp'
        }
        assertTrue dep1Task != null

        Task dep2Task = project.tasks.findByName('runUITest02testB').dependsOn.find {
            it instanceof DefaultTask && it.name == 'buildUITestApp'
        }
        assertTrue dep2Task != null

        Task dep3Task = project.tasks.findByName('runUITests').dependsOn.find {
            it instanceof DefaultTask && it.name == 'buildUITestApp'
        }
        assertTrue dep3Task != null

        Task dep4Task = project.tasks.findByName('runUITests').dependsOn.find {
            it instanceof DefaultTask && it.name == 'runUITest01testA'
        }
        assertTrue dep4Task != null

        Task dep5Task = project.tasks.findByName('runUITests').dependsOn.find {
            it instanceof DefaultTask && it.name == 'runUITest02testB'
        }
        assertTrue dep5Task != null
    }

    Project getProjectConfigure() {
        Project _project = ProjectBuilder.builder().build()
        _project.apply plugin: MobilePlugin
        _project.ctcoMobile {
            platform = 'xcode'
            xcode {
                automaticConfiguration = false
                environment name: 'DEV', target: 'iGREW DEV'
            }
        }
        MobilePluginUtil.detectAndConfigurePlatform(_project)
        return _project
    }

    */

}
