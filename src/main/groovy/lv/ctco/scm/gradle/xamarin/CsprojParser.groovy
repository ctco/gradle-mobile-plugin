/*
 * @(#)MsBuildConfigurationParser.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin

import org.apache.commons.lang3.StringUtils

/**
 *
 * MS Build file parses. Extracts only relevant values from the XML file. It is a very dumb parsing mechanism
 * it ignores property overrides and does not parse PropertyGroup conditions, but it makes the job done.
 *
 */
public class CsprojParser {

    File file

    CsprojParser(File file) {
        this.file = file
    }

    public Csproj parse() {
        def project = new XmlSlurper().parse(file)

        String assemblyName = project.PropertyGroup[0].AssemblyName
        String releaseVersion = project.PropertyGroup[0].ReleaseVersion
        StringUtils.trimToNull(releaseVersion)

        Map<String, String> outputMapping = new HashMap<String, String>()

        project.PropertyGroup.depthFirst().collect { def group ->
            String condition = group.@Condition
            def m = condition =~ " '\\\$\\(XcodeConfiguration\\)\\|\\\$\\(Platform\\)' == '([^']+)' "
            if (m.matches()) {
                String configurationName = m.group(1)
                String outputPath = group.OutputPath.text().replace('\\'.toCharacter(), File.separatorChar)

                outputMapping[configurationName] = outputPath
            }
        }

        new Csproj(file, assemblyName, releaseVersion, outputMapping)
    }

}
