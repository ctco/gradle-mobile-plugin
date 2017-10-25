package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.mobile.utils.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XandroidConfiguration {

    private boolean automaticConfiguration = false;
    private File solutionFile;
    private File projectFile;
    private String unitTestProject;

    private String signingKeystore;
    private String signingCertificateAlias;
    private String androidVersionCode;

    private List<Environment> environments = new ArrayList<>();
    private List<Profile> profiles = new ArrayList<>();

    public boolean isAutomaticConfiguration() {
        return automaticConfiguration;
    }

    public void setAutomaticConfiguration(boolean automaticConfiguration) {
        this.automaticConfiguration = automaticConfiguration;
    }

    public File getSolutionFile() {
        return solutionFile;
    }

    public void setSolutionFile(File solutionFile) {
        this.solutionFile = solutionFile;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public String getUnitTestProject() {
        return unitTestProject;
    }

    public void setUnitTestProject(String unitTestProject) {
        this.unitTestProject = unitTestProject;
    }

    public String getSigningKeystore() {
        return signingKeystore;
    }

    public void setSigningKeystore(String signingKeystore) {
        this.signingKeystore = signingKeystore;
    }

    public String getSigningCertificateAlias() {
        return signingCertificateAlias;
    }

    public void setSigningCertificateAlias(String signingCertificateAlias) {
        this.signingCertificateAlias = signingCertificateAlias;
    }

    public String getAndroidVersionCode() {
        return androidVersionCode;
    }

    public void setAndroidVersionCode(String androidVersionCode) {
        this.androidVersionCode = androidVersionCode;
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

    public boolean isValid() {
        boolean result = true;
        if (solutionFile == null) {
            result = false;
        }
        if (projectFile == null) {
            result = false;
        }
        if (environments.isEmpty()) {
            result = false;
        }
        return result;
    }

    @Override
    public String toString() {
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Xamarin.Android{");
        sb.append(ls);
        sb.append("  automaticConfiguration=");
        sb.append(automaticConfiguration);
        sb.append(ls);
        if (solutionFile != null) {
            sb.append("  solutionFile=");
            sb.append(solutionFile);
            sb.append(ls);
        }
        if (projectFile != null) {
            sb.append("  projectFile=");
            sb.append(projectFile);
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
