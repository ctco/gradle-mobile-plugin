/*
 * @(#)SolutionParserTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import org.codehaus.jparsec.Parser
import org.codehaus.jparsec.functors.Pair

import org.junit.Test

import static org.junit.Assert.assertEquals

class SolutionParserTest {

    @Test
    public void createProjectUnitParser() {
        SolutionParser parser = new SolutionParser(null)

        assertEquals 'ABC-DEF', parser.createProjectUnitParser().parse('Project("ABC-DEF")')
    }

    @Test
    public void createCommaSeparatedStringsParser() {
        SolutionParser parser = new SolutionParser(null)

        List<String> expected = ['Item1', 'Item2', 'Item3']

        assertEquals expected, parser.createCommaSeparatedStringsParser().parse('"Item1","Item2", "Item3"')
    }

    @Test
    public void createProjectSectionParser() {
        SolutionParser parser = new SolutionParser(null)

        SlnProjectSection result =
            parser.createProjectSectionParser().parse('Project("{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}") =' +
                ' "Common.Tests", "..\\..\\Modules\\Common\\trunk\\Common.Tests\\Common.Tests.csproj",' +
                ' "{E976C9D7-EEBE-4FBA-91B2-CCC162BA67F4}"\n' +
                'EndProject')

        assertEquals '{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}', result.type
    }

    @Test
    public void createPropertyParser() {
        Pair<String,String> expected =
            new Pair('{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg', 'Debug|Any CPU')
        Pair<String,String> actual = SolutionParser.createPropertyParser().parse(
                '{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg = Debug|Any CPU')

        assertEquals actual, expected
    }

    @Test
    public void createGlobalSectionUnitParser() {
        Parser<String> parser = SolutionParser.createGlobalSectionUnitParser()
        assertEquals parser.parse('GlobalSection(SolutionConfigurationPlatforms) = preSolution'),
                'SolutionConfigurationPlatforms'
        assertEquals parser.parse('GlobalSection(ProjectConfigurationPlatforms) = postSolution'),
                'ProjectConfigurationPlatforms'
    }

    @Test
    public void createGlobalSectionParser() {
        Parser<SlnGlobalSection> parser = SolutionParser.createGlobalSectionParser()

        SlnGlobalSection actual = parser.parse('GlobalSection(SolutionConfigurationPlatforms) = preSolution\n' +
                '\t\tDebug|Any CPU = Debug|Any CPU\n' +
                '\t\tDebug|ARM = Debug|ARM\n' +
                '\t\tDebug|Mixed Platforms = Debug|Mixed Platforms\n' +
                '\tEndGlobalSection')

        SlnGlobalSection expected = new SlnGlobalSection('SolutionConfigurationPlatforms')
        expected.putProperty('Debug|Any CPU', 'Debug|Any CPU')
        expected.putProperty('Debug|ARM', 'Debug|ARM')
        expected.putProperty('Debug|Mixed Platforms', 'Debug|Mixed Platforms')

        assertEquals actual, expected

    }

    @Test
    public void createGlobalSectionListParser() {
        List<SlnGlobalSection> actual = SolutionParser.createGlobalSectionListParser().parse('Global\n' +
                '\tGlobalSection(SolutionConfigurationPlatforms) = preSolution\n' +
                '\t\tDebug|Any CPU = Debug|Any CPU\n' +
                '\tEndGlobalSection\n' +
                '\tGlobalSection(ProjectConfigurationPlatforms) = postSolution\n' +
                '\t\t{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg = Debug|Any CPU\n' +
                '\tEndGlobalSection\n' +
                'EndGlobal')

        SlnGlobalSection expectedSection1 = new SlnGlobalSection('SolutionConfigurationPlatforms')
        expectedSection1.putProperty('Debug|Any CPU', 'Debug|Any CPU')
        SlnGlobalSection expectedSection2 = new SlnGlobalSection('ProjectConfigurationPlatforms')
        expectedSection2.putProperty('{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg', 'Debug|Any CPU')
        List<SlnGlobalSection> expected = new LinkedList<SlnGlobalSection>()
        expected.add(expectedSection1)
        expected.add(expectedSection2)

        assertEquals actual.size(), expected.size()
        assertEquals actual[0].name, expected[0].name
        assertEquals actual[0], expected[0]
        assertEquals actual[1].name, expected[1].name
        assertEquals actual[1], expected[1]
    }

    @Test
    public void createSolutionParser() {
        Solution actual = SolutionParser.createSolutionParser().parse(
                '\n' +
                'Microsoft Visual Studio Solution File, Format Version 12.00\n' +
                '# Visual Studio 2012\n' +
                'Project("{2150E333-8FDC-42A3-9474-1A3956D46DE8}") = "Space", "Space", "{8CB983C9-8FAC-4A51-A373-6B5177C209A2}"\n' +
                'EndProject\n' +
                'Project("{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}") = "SpaceY.WinRT", "SpaceY.WinRT\\SpaceY.WinRT.csproj", "{5CC6F493-723D-448B-9039-45786BBC7272}"\n' +
                'EndProject\n' +
                'Global\n' +
                '\tGlobalSection(SolutionConfigurationPlatforms) = preSolution\n' +
                '\t\tDebug|Any CPU = Debug|Any CPU\n' +
                '\tEndGlobalSection\n' +
                '\tGlobalSection(ProjectConfigurationPlatforms) = postSolution\n' +
                '\t\t{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg = Debug|Any CPU\n' +
                '\tEndGlobalSection\n' +
                'EndGlobal')

        SlnProjectSection expectedProjectSection1 = new SlnProjectSection('Space',
                '{2150E333-8FDC-42A3-9474-1A3956D46DE8}', 'Space', '{8CB983C9-8FAC-4A51-A373-6B5177C209A2}')
        SlnProjectSection expectedProjectSection2 = new SlnProjectSection('SpaceY.WinRT',
                '{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}', 'SpaceY.WinRT\\SpaceY.WinRT.csproj',
                '{5CC6F493-723D-448B-9039-45786BBC7272}')
        List<SlnProjectSection> expectedProjectSections = new LinkedList<SlnProjectSection>()
        expectedProjectSections.add(expectedProjectSection1)
        expectedProjectSections.add(expectedProjectSection2)

        SlnGlobalSection expectedSection1 = new SlnGlobalSection('SolutionConfigurationPlatforms')
        expectedSection1.putProperty('Debug|Any CPU', 'Debug|Any CPU')
        SlnGlobalSection expectedSection2 = new SlnGlobalSection('ProjectConfigurationPlatforms')
        expectedSection2.putProperty('{04D2A389-A805-4427-BF40-F7E758131D3E}.Ad-Hoc|iPhone.ActiveCfg', 'Debug|Any CPU')
        List<SlnGlobalSection> expectedGlobalSections = new LinkedList<SlnGlobalSection>()
        expectedGlobalSections.add(expectedSection1)
        expectedGlobalSections.add(expectedSection2)

        Solution expected = new Solution(expectedProjectSections, expectedGlobalSections)

        assertEquals actual, expected
    }

}
