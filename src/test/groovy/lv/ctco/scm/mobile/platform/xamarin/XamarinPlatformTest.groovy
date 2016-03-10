/*
 * @(#)XamarinPlatformTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Environment

import org.gmock.GMockController
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class XamarinPlatformTest {

    private GMockController gmc
    private File mockSolutionFile
    Solution solution
    Csproj configuration

    @Before
    public void setUp() {
        gmc = new GMockController()
        mockSolutionFile = createMockSolutionFile()
    }

    @Test
    public void solutionFileUndefined() {
        XamarinExtension extension = new XamarinExtension()
        XamarinPlatform platform = createPlatform()
        try {
            platform.configure(extension, null)
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'solutionFile for ctcoMobile.xamarin extension is not defined.'
            return
        }
        fail('Exception expected, solutionFile is not defined.')
    }

    @Test
    public void autodetectMultiTarget() {
        Environment expectedEnvironment0 = new Environment('DEV', 'TestY DEV|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY DEV').getAbsoluteFile())
        Environment expectedEnvironment1 = new Environment('TRAIN', 'TestY TRAIN|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY TRAIN').getAbsoluteFile())
        Environment expectedEnvironment2 = new Environment('UAT', 'TestY UAT|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY UAT').getAbsoluteFile())

        initMocks(['DEV', 'TRAIN', 'UAT'], false)
        gmc.play {
            XamarinExtension extension = createXamarinExtension()

            XamarinPlatform platform = createPlatform()
            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

            assertEquals extension.environments.size(), 3
            assertEquals extension.environments['DEV'], expectedEnvironment0
            assertEquals extension.environments['TRAIN'], expectedEnvironment1
            assertEquals extension.environments['UAT'], expectedEnvironment2
        }
    }

    @Test
    public void autodetectSingleEnvironmentTarget() {
        Environment expectedEnvironment = new Environment('DEV', 'TestY DEV|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY DEV').getAbsoluteFile())

        initMocks(['DEV'], false)
        gmc.play {
            XamarinExtension extension = createXamarinExtension()
            XamarinPlatform platform = createPlatform()

            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'),
                configuration)

            assertEquals extension.environments.size(), 1
            assertEquals extension.environments['DEV'], expectedEnvironment
        }
    }

    @Test
    public void autodetectNoEnvironmentTargets() {
        Environment expectedEnvironment =
            new Environment('DEFAULT', 'Ad-Hoc|iPhone', new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/Ad-Hoc').getAbsoluteFile())

        initMocks([], true)
        gmc.play {
            XamarinExtension extension = createXamarinExtension()
            XamarinPlatform platform = createPlatform()
            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'),
                configuration)

            assertEquals extension.environments.size(), 1
            assertEquals extension.environments['DEFAULT'], expectedEnvironment
        }
    }

    @Test
    public void autodetectNoEnvironments() {
        initMocks([], false)
        try {
            gmc.play {
                XamarinExtension extension = createXamarinExtension()
                XamarinPlatform platform = createPlatform()
                platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)
            }
        } catch (Exception e) {
            assert(e.getMessage().equals("No environments detected, no build is going to be performed!"))
            return
        }
        fail('Exception expected, no environments detected.')
    }

    @Test
    public void autodetectEnvironmentsPartiallyDefinedSameName() {
        Environment expectedEnvironment0 = new Environment('DEV', 'My Config|iPhone', new File('dummy'))
        Environment expectedEnvironment1 = new Environment('TRAIN', 'TestY TRAIN|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY TRAIN').getAbsoluteFile())
        Environment expectedEnvironment2 = new Environment('UAT', 'TestY UAT|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY UAT').getAbsoluteFile())

        initMocks(['DEV', 'TRAIN', 'UAT'], false)
        gmc.play {
            XamarinExtension extension = createXamarinExtension()
            extension.environment name: 'DEV', configuration: 'My Config|iPhone', outputPath: 'dummy'

            XamarinPlatform platform = createPlatform()

            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

            assertEquals extension.environments.size(), 3
            assertEquals extension.environments['DEV'], expectedEnvironment0
            assertEquals extension.environments['TRAIN'], expectedEnvironment1
            assertEquals extension.environments['UAT'], expectedEnvironment2
        }
    }

    @Test
    public void autodetectEnvironmentsPartiallyDefinedSameConfiguration() {
        Environment expectedEnvironment0 = new Environment('DEV', 'TestY DEV|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY DEV').getAbsoluteFile())
        Environment expectedEnvironment1 = new Environment('MYENV', 'TestY TRAIN|iPhone', new File('dummy'))
        Environment expectedEnvironment2 = new Environment('UAT', 'TestY UAT|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY UAT').getAbsoluteFile())

        initMocks(['DEV', 'TRAIN', 'UAT'], false)
        gmc.play {
            XamarinExtension extension = createXamarinExtension()
            extension.environment name: 'MYENV', configuration: 'TestY TRAIN|iPhone', outputPath: 'dummy'

            XamarinPlatform platform = createPlatform()

            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

            assertEquals extension.environments.size(), 3
            assertEquals extension.environments['DEV'], expectedEnvironment0
            assertEquals extension.environments['MYENV'], expectedEnvironment1
            assertEquals extension.environments['UAT'], expectedEnvironment2
        }

    }

    public void initMocks(List<String> environments, boolean defaultEnvironment) {
        solution = createSolution(environments, defaultEnvironment)
        configuration = createMockMsBuildConfiguration(environments, defaultEnvironment)
    }

    public XamarinExtension createXamarinExtension() {
        XamarinExtension extension = new XamarinExtension()
        extension.solutionFile = mockSolutionFile
        return extension
    }

    public File createMockSolutionFile() {
        File mockSolutionFile = gmc.mock(File)
        mockSolutionFile.name.returns("TestY.sln").times(1)
        return mockSolutionFile
    }

    public XamarinPlatform createPlatform() {
        return new XamarinPlatform(ProjectBuilder.builder().build())
    }

    public Csproj createMockMsBuildConfiguration(List<String> environments, boolean defaultEnvironment) {
        def mockMsBuildDirectory = gmc.mock(File)
        mockMsBuildDirectory.absolutePath.returns('/Users/xamarin/solution/TestY.iOS')

        def mockMsBuildConfiguration = gmc.mock(Csproj)
        mockMsBuildConfiguration.directory.returns(mockMsBuildDirectory)
        mockMsBuildConfiguration.assemblyName.returns('TesyYiOS')
        environments.each {
            mockMsBuildConfiguration.getOutputPathForConfiguration("TestY $it|iPhone").returns("bin/iPhone/TestY $it").atMost(1)
        }
        if (defaultEnvironment) {
            mockMsBuildConfiguration.getOutputPathForConfiguration('Ad-Hoc|iPhone').returns('bin/iPhone/Ad-Hoc').atMost(1)
        }
        return mockMsBuildConfiguration
    }

    public Solution createSolution(List<String> environments, boolean defaultEnvironment) {
        LinkedList<SlnProjectSection> projectSections = new LinkedList<SlnProjectSection>()
        projectSections.add(new SlnProjectSection('TestY.iOS', '{111}', 'TestY.iOS/TestY.iOS.csproj', '{211}'))

        SlnGlobalSection projectConfigurationPlatforms = new SlnGlobalSection('ProjectConfigurationPlatforms')
        environments.each {
            projectConfigurationPlatforms.putProperty("{211}.TestY $it|iPhone.Build.0", "TestY $it|iPhone")
        }
        projectConfigurationPlatforms.putProperty("{211}.Ad-Hoc|iPhone.Build.0", "Ad-Hoc|iPhone")

        SlnGlobalSection solutionConfigurationPlatforms = new SlnGlobalSection('SolutionConfigurationPlatforms')
        environments.each {
            solutionConfigurationPlatforms.putProperty("TestY $it|iPhone", "TestY $it|iPhone")
        }
        if (defaultEnvironment) {
            solutionConfigurationPlatforms.putProperty("Ad-Hoc|iPhone", "Ad-Hoc|iPhone")
        }

        LinkedList<SlnGlobalSection> globalSections = new LinkedList<SlnGlobalSection>()
        globalSections.add(projectConfigurationPlatforms)
        globalSections.add(solutionConfigurationPlatforms)

        return new Solution(projectSections, globalSections)
    }

}
