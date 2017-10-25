/*
 * @(#)XamarinPlatformTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin

import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Ignore
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

    @Ignore
    @Test
    public void solutionFileUndefined() {
        XamarinConfiguration configuration = new XamarinConfiguration()
        XamarinPlatform platform = createPlatform()
        try {
            platform.configure(configuration, null)
        } catch (IOException e) {
            assertEquals e.getMessage(), 'solutionFile for ctcoMobile.xamarin extension is not defined'
            return
        }
        fail('Exception expected, solutionFile is not defined.')
    }

    @Ignore
    @Test
    public void autodetectNoEnvironmentTargets() {
        Environment expectedEnvironment = new Environment()
        expectedEnvironment.setName("DEFAULT")
        expectedEnvironment.setConfiguration("Ad-Hoc")
        expectedEnvironment.setPlatform("iPhone")

        initMocks()
        XamarinExtension extension = createXamarinExtension()
        XamarinPlatform platform = createPlatform()
        platform.performAutomaticEnvironmentConfiguration(extension, solution, configuration)

        assertEquals(1, extension.environments.size())
        assertEquals(expectedEnvironment, extension.getEnvironments().get(0))
    }

    //

    private void initMocks() {
        solution = createSolution()
        configuration = createMockCsproj()
    }

    private XamarinExtension createXamarinExtension() {
        XamarinExtension extension = new XamarinExtension()
        extension.solutionFile = mockSolutionFile
        return extension
    }

    private XamarinPlatform createPlatform() {
        return new XamarinPlatform(ProjectBuilder.builder().build())
    }

    private Csproj createMockCsproj() {
        Map<String, String> outputMapping = new HashMap<>()
        outputMapping.put("Debug|iPhone", "bin/iPhone/Debug");
        outputMapping.put("Ad-Hoc|iPhone", "bin/iPhone/Ad-Hoc");
        outputMapping.put("Release|iPhone", "bin/iPhone/Release");
        Csproj csproj = new Csproj(new File("/Users/xamarin/solution/TestY.iOS/TestY.iOS.csproj"), "TestYiOS", null, outputMapping)
    }

    private Solution createSolution() {
        LinkedList<SlnProjectSection> projectSections = new LinkedList<SlnProjectSection>()
        projectSections.add(new SlnProjectSection('TestY.iOS', '{111}', 'TestY.iOS/TestY.iOS.csproj', '{211}'))

        SlnGlobalSection projectConfigurationPlatforms = new SlnGlobalSection('ProjectConfigurationPlatforms')
        projectConfigurationPlatforms.putProperty("{211}.Ad-Hoc|iPhone.Build.0", "Ad-Hoc|iPhone")
        projectConfigurationPlatforms.putProperty("{211}.Release|iPhone.Build.0", "Release|iPhone")

        SlnGlobalSection solutionConfigurationPlatforms = new SlnGlobalSection('SolutionConfigurationPlatforms')
        solutionConfigurationPlatforms.putProperty("Debug|iPhone", "Debug|iPhone")
        solutionConfigurationPlatforms.putProperty("Ad-Hoc|iPhone", "Ad-Hoc|iPhone")
        solutionConfigurationPlatforms.putProperty("Release|iPhone", "Release|iPhone")

        LinkedList<SlnGlobalSection> globalSections = new LinkedList<SlnGlobalSection>()
        globalSections.add(projectConfigurationPlatforms)
        globalSections.add(solutionConfigurationPlatforms)

        return new Solution(projectSections, globalSections)
    }

}
