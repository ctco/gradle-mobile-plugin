/*
 * @(#)XamarinPlatformTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Environment

import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class XamarinPlatformTest {

    private File mockSolutionFile
    Solution solution
    Csproj configuration

    @Before
    public void setUp() {
        mockSolutionFile = new File("TestY.sln")
    }

    @Test
    public void solutionFileUndefined() {
        XamarinExtension extension = new XamarinExtension()
        XamarinPlatform platform = createPlatform()
        try {
            platform.configure(extension, null)
        } catch (IOException e) {
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
        XamarinExtension extension = createXamarinExtension()
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

        assertEquals extension.environments.size(), 3
        assertEquals extension.environments['DEV'], expectedEnvironment0
        assertEquals extension.environments['TRAIN'], expectedEnvironment1
        assertEquals extension.environments['UAT'], expectedEnvironment2
    }

    @Test
    public void autodetectSingleEnvironmentTarget() {
        Environment expectedEnvironment = new Environment('DEV', 'TestY DEV|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY DEV').getAbsoluteFile())

        initMocks(['DEV'], false)
        XamarinExtension extension = createXamarinExtension()
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

        assertEquals extension.environments.size(), 1
        assertEquals extension.environments['DEV'], expectedEnvironment
    }

    @Test
    public void autodetectNoEnvironmentTargets() {
        Environment expectedEnvironment =
            new Environment('DEFAULT', 'Ad-Hoc|iPhone', new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/Ad-Hoc').getAbsoluteFile())

        initMocks([], true)
        XamarinExtension extension = createXamarinExtension()
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'),
            configuration)

        assertEquals extension.environments.size(), 1
        assertEquals extension.environments['DEFAULT'], expectedEnvironment
    }

    @Test
    public void autodetectNoEnvironments() {
        initMocks([], false)
        try {
            XamarinExtension extension = createXamarinExtension()
            XamarinPlatform platform = createPlatform()
            platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)
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
        XamarinExtension extension = createXamarinExtension()
        extension.environment name: 'DEV', configuration: 'My Config|iPhone', outputPath: 'dummy'
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

        assertEquals extension.environments.size(), 3
        assertEquals extension.environments['DEV'], expectedEnvironment0
        assertEquals extension.environments['TRAIN'], expectedEnvironment1
        assertEquals extension.environments['UAT'], expectedEnvironment2
    }

    @Test
    public void autodetectEnvironmentsPartiallyDefinedSameConfiguration() {
        Environment expectedEnvironment0 = new Environment('DEV', 'TestY DEV|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY DEV').getAbsoluteFile())
        Environment expectedEnvironment1 = new Environment('MYENV', 'TestY TRAIN|iPhone', new File('dummy'))
        Environment expectedEnvironment2 = new Environment('UAT', 'TestY UAT|iPhone',
                new File('/Users/xamarin/solution/TestY.iOS/bin/iPhone/TestY UAT').getAbsoluteFile())

        initMocks(['DEV', 'TRAIN', 'UAT'], false)
        XamarinExtension extension = createXamarinExtension()
        extension.environment name: 'MYENV', configuration: 'TestY TRAIN|iPhone', outputPath: 'dummy'
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticConfiguration(extension, solution, solution.getProject('TestY.iOS'), configuration)

        assertEquals extension.environments.size(), 3
        assertEquals extension.environments['DEV'], expectedEnvironment0
        assertEquals extension.environments['MYENV'], expectedEnvironment1
        assertEquals extension.environments['UAT'], expectedEnvironment2
    }

    public void initMocks(List<String> environments, boolean defaultEnvironment) {
        solution = createSolution(environments, defaultEnvironment)
        configuration = createMockCsproj(environments, defaultEnvironment)
    }

    public XamarinExtension createXamarinExtension() {
        XamarinExtension extension = new XamarinExtension()
        extension.solutionFile = mockSolutionFile
        return extension
    }

    public XamarinPlatform createPlatform() {
        return new XamarinPlatform(ProjectBuilder.builder().build())
    }

    public Csproj createMockCsproj(List<String> environments, boolean defaultEnvironment) {
        Map<String, String> outputMapping = new HashMap<>()
        for (String env : environments) {
            outputMapping.put("TestY "+env+"|iPhone", "bin/iPhone/TestY "+env);
        }
        if (defaultEnvironment) {
            outputMapping.put("Ad-Hoc|iPhone", "bin/iPhone/Ad-Hoc");
        }
        Csproj csproj = new Csproj(new File("/Users/xamarin/solution/TestY.iOS/TestY.iOS.csproj"), "TestYiOS", null, outputMapping)
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
