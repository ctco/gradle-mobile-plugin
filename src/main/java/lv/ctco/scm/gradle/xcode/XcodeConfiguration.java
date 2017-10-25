package lv.ctco.scm.gradle.xcode;

import lv.ctco.scm.mobile.utils.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XcodeConfiguration {

    private boolean automaticConfiguration = true;
    private File projectFile;
    private String projectName;
    private String unitTestScheme;

    private List<Environment> environments = new ArrayList<>();
    private List<Profile> profiles = new ArrayList<>();

    public boolean isAutomaticConfiguration() {
        return automaticConfiguration;
    }

    public void setAutomaticConfiguration(boolean automaticConfiguration) {
        this.automaticConfiguration = automaticConfiguration;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUnitTestScheme() {
        return unitTestScheme;
    }

    public void setUnitTestScheme(String unitTestScheme) {
        this.unitTestScheme = unitTestScheme;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public List<Profile> getSpecificProfiles(String envName, String scope) {
        List<Profile> filteredProfiles = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile.getEnvironment().equalsIgnoreCase(envName) && profile.getScope().equals(scope)) {
                filteredProfiles.add(profile);
            }
        }
        Collections.sort(filteredProfiles);
        return filteredProfiles;
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
        Collections.sort(profiles);
    }

    @Override
    public String toString() {
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Xcode{");
        sb.append(ls);
        sb.append("  automaticConfiguration=");
        sb.append(automaticConfiguration);
        sb.append(ls);
        if (projectFile != null) {
            sb.append("  projectFile=");
            sb.append(projectFile);
            sb.append(ls);
        }
        if (projectName != null) {
            sb.append("  projectName=");
            sb.append(projectName);
            sb.append(ls);
        }
        if (unitTestScheme != null) {
            sb.append("  unitTestScheme=");
            sb.append(unitTestScheme);
            sb.append(ls);
        }
        if (environments.isEmpty()) {
            sb.append("  environments[]");
            sb.append(ls);
        } else {
            sb.append("  environments[");
            sb.append(ls);
            for (Environment environment : environments) {
                sb.append("    ");
                sb.append(environment);
                sb.append(ls);
            }
            sb.append("  ]");
            sb.append(ls);
        }
        if (profiles.isEmpty()) {
            sb.append("  profiles[]");
            sb.append(ls);
        } else {
            sb.append("  profiles[");
            sb.append(ls);
            for (Profile profile : profiles) {
                sb.append("    ");
                sb.append(profile);
                sb.append(ls);
            }
            sb.append("  ]");
            sb.append(ls);
        }
        sb.append(']');
        return sb.toString();
    }

}
