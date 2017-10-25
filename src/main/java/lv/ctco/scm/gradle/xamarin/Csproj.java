/*
 * @(#)MsBuildConfiguration.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import java.io.File;
import java.util.Map;

/** A parsed MSBuild configuration file .csproj. */
public class Csproj {

    private Map<String, String> outputMapping;

    private File file;
    private String assemblyName;
    private String releaseVersion;

    Csproj(File file, String assemblyName, String releaseVersion, Map<String, String> outputMapping) {
        this.file = file;
        this.outputMapping = outputMapping;
        this.assemblyName = assemblyName;
        this.releaseVersion = releaseVersion;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public File getFile() {
        return file;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public String getOutputPathForConfiguration(String projectConfigurationName) {
        return outputMapping.get(projectConfigurationName);
    }

    public File getDirectory() {
        return file.getParentFile();
    }

}
