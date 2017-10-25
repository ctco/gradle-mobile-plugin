/*
 * @(#)Solution.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin

/**
 * MS VisualStudio solution file representation. Contains information from project and global sections.
 */
class Solution {

    protected List<SlnProjectSection> projectSections
    protected List<SlnGlobalSection> globalSections

    Solution(List<SlnProjectSection> projectSections, List<SlnGlobalSection> globalSections) {
        this.projectSections = projectSections
        this.globalSections = globalSections
    }

    /**
     * @return A set of property names in SolutionConfigurationPlatformsSection
     */
    public Set<String> getSolutionConfigurations() {
        SlnGlobalSection section = getSolutionConfigurationsSection()
        if (section == null) {
            throw new IOException("There is no SolutionConfigurationPlatforms section")
        }
        return section.propertyNames
    }

    /**
     * @return SlnGlobalSection "SolutionConfigurationPlatforms"
     */
    public SlnGlobalSection getSolutionConfigurationsSection() {
        return globalSections.find { it.name == 'SolutionConfigurationPlatforms' }
    }

    /**
     * @return SlnGlobalSection "ProjectConfigurationPlatforms"
     */
    public SlnGlobalSection getProjectConfigurationPlatformsSection() {
        return globalSections.find { it.name == 'ProjectConfigurationPlatforms' }
    }

    /**
     * Determines of there is a project with a given name in ProjectSections
     * @param name Name of the project to find
     * @return true if the project is found
     */
    public boolean containsProject(String name) {
        return getProject(name) != null
    }

    /**
     * Returns a SlnProjectSection with the given name
     * @param name A name of SlnProjectSection to search for
     * @return SlnProjectSection with the given name
     */
    public SlnProjectSection getProject(String name) {
        SlnProjectSection project = projectSections.find { it.name == name }
        if (project == null) {
            throw new IOException("Project $name not found in solution $name")
        }
        return project
    }

    /**
     * Determines what project configuration maps to a given solution configuration.
     *
     * @param solutionConfigurationName Solution configuration name
     * @param projectSection Project section
     * @return The name of the found project sections
     */
    public String getConfigurationMappingForProject(String solutionConfigurationName, SlnProjectSection projectSection) {
        String propertyName = "${projectSection.uID}.${solutionConfigurationName}.Build.0"

        SlnGlobalSection section = projectConfigurationPlatformsSection
        if (!section.propertyExists(propertyName)) {
            throw new IOException("Property ${propertyName} does not exist in ${section.name} section," +
                    " unable to determine project configuration in ${projectSection.name} project that corresponds" +
                    " to $solutionConfigurationName solution configuration.")
        }

        return section.getProperty(propertyName)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Solution solution = (Solution)o

        if (globalSections != solution.globalSections) return false
        if (projectSections != solution.projectSections) return false

        return true
    }

    int hashCode() {
        int result
        result = projectSections.hashCode()
        result = 31 * result + globalSections.hashCode()
        return result
    }

}
