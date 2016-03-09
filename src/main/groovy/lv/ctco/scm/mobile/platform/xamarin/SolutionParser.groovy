/*
 * @(#)SolutionParser.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import org.codehaus.jparsec.Parser
import org.codehaus.jparsec.Parsers
import org.codehaus.jparsec.Scanners
import org.codehaus.jparsec.functors.Map2
import org.codehaus.jparsec.functors.Pair
import org.codehaus.jparsec.pattern.Pattern
import org.codehaus.jparsec.pattern.Patterns

import org.gradle.api.GradleException

/**
 *
 * MS Visual Studion solution file parser. This parser is quite simple and is not error-proof in case of
 * manual solution file editing. It implement parsing of only the basic configurations like Project and Global
 * tags.
 *
 * Please have a look a the lv.ctco.scm.mobile.xamarin.SolutionParserTest for more information.
 *
 */

public class SolutionParser {

    protected File file

    public SolutionParser(File file) {
        this.file = file
    }

    /**
     * Parses a solution file.
     *
     * @return Object representation of the parsed solution file.
     */
    Solution parse() {
        if (!file.exists()) {
            throw new GradleException("Solution file $file.absolutePath does not exist")
        }
        createSolutionParser().parse(file.text)
    }

    protected static final Parser<String> STRING_PARSER =
            Scanners.DOUBLE_QUOTE_STRING.map(
                    [
                            map: { String str -> str.length() > 2 ? str[1..str.length() - 2] : '' }
                    ] as org.codehaus.jparsec.functors.Map<String, String>
            )

    protected static Parser<String> createProjectUnitParser() {
        Scanners.string('Project').next(
                Parsers.between(
                        Scanners.isChar('('.toCharacter()),
                        STRING_PARSER,
                        Scanners.isChar(')'.toCharacter()),
                )
        )
    }

    protected static Parser<List<String>> createCommaSeparatedStringsParser() {
        Parsers.sequence(
                Scanners.WHITESPACES.optional(),
                STRING_PARSER,
        ).sepBy(Scanners.isChar(','.toCharacter()))
    }

    protected static final Parser<Void> ASSIGNMENT = Parsers.sequence(
            Scanners.WHITESPACES,
            Scanners.isChar('='.toCharacter()),
            Scanners.WHITESPACES
    )

    protected static Parser<SlnProjectSection> createProjectSectionParser() {
        Parsers.sequence(createProjectUnitParser().followedBy(ASSIGNMENT), createCommaSeparatedStringsParser(), [
                map: { String projectType, List<String> values ->
                    new SlnProjectSection(values[0], projectType, values[1], values[2])
                }
        ] as Map2<String, List<String>, SlnProjectSection>).
                followedBy(Scanners.WHITESPACES).
                followedBy(Scanners.string('EndProject'))
    }

    protected static Parser<List<SlnProjectSection>> createProjectSectionListParser() {
        Parsers.or(
                createProjectSectionParser().followedBy(Scanners.WHITESPACES),
                (Parser<SlnProjectSection>)Parsers.never()
        ).many()
    }

    protected static Parser<Pair<String, String>> createPropertyParser() {
        Pattern pattern = Patterns.regex('[^=&&[^\\r\\n\\f]]+')
        Parser<String> identifierParser = Scanners.pattern(pattern, 'identifier').source().map(
                new org.codehaus.jparsec.functors.Map<String, String>() {

                    String map(String from) {
                        return from.trim()
                    }
                })
        Parsers.tuple(identifierParser.followedBy(Scanners.isChar('='.toCharacter())), identifierParser)
    }

    protected static Parser<String> createGlobalSectionUnitParser() {
        Parser<String> sectionNameParser = Scanners.pattern(Patterns.regex('[^)]+'), 'section name').source()
        Scanners.string('GlobalSection(').
                next(sectionNameParser).
                followedBy(
                        Parsers.sequence(
                                Scanners.isChar(')'.toCharacter()),
                                Scanners.WHITESPACES,
                                Scanners.isChar('='.toCharacter()),
                                Scanners.WHITESPACES
                        )
                ).followedBy(
                Parsers.or(
                        Scanners.string('preSolution'),
                        Scanners.string('postSolution')
                )
        )
    }

    protected static Parser<SlnGlobalSection> createGlobalSectionParser() {
        Parser<List<Pair<String, String>>> propertyListParser =
                Parsers.or(
                        createPropertyParser().followedBy(Scanners.WHITESPACES),
                        Parsers.never() as Parser<Pair<String, String>>
                ).many()
        Parsers.sequence(
                createGlobalSectionUnitParser().followedBy(Scanners.WHITESPACES),
                propertyListParser,
                new Map2<String, List<Pair<String, String>>, SlnGlobalSection>() {

                    SlnGlobalSection map(String sectionName, List<Pair<String, String>> properties) {
                        SlnGlobalSection section = new SlnGlobalSection(sectionName)
                        properties.each { Pair<String, String> property ->
                            section.putProperty(property.a, property.b)
                        }
                        return section
                    }
                }
        ).followedBy(Scanners.string('EndGlobalSection'))
    }

    protected static Parser<List<SlnGlobalSection>> createGlobalSectionListParser() {
        Parsers.between(
                Scanners.string('Global').followedBy(Scanners.WHITESPACES),
                Parsers.or(createGlobalSectionParser().followedBy(Scanners.WHITESPACES),
                        (Parser<SlnGlobalSection>)Parsers.never()).many(),
                Scanners.string('EndGlobal')
        )
    }

    protected static Parser<Solution> createSolutionParser() {
        Scanners.WHITESPACES.
                followedBy(Scanners.string('Microsoft Visual Studio Solution File, Format Version 12.00')).optional().
                followedBy(Scanners.WHITESPACES).
                followedBy(Scanners.string('# Visual Studio 2012')).optional().
                followedBy(Scanners.WHITESPACES).next(Parsers.sequence(
                createProjectSectionListParser(),
                createGlobalSectionListParser(),
                new Map2<List<SlnProjectSection>, List<SlnGlobalSection>, Solution>() {

                    Solution map(List<SlnProjectSection> projectSections, List<SlnGlobalSection> globalSections) {
                        return new Solution(projectSections, globalSections)
                    }
                }
        )).followedBy(Scanners.WHITESPACES.optional())
    }

}
