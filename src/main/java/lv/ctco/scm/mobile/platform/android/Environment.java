/*
 * @(#)Environment.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.android;

import java.io.File;

final class Environment {

    private String name;
    private String configuration;
    private String buildVersion;
    private String assemblyName;
    private String assemblyType;
    private File assemblyPath;
    private File manifestPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public String getAssemblyType() {
        return assemblyType;
    }

    public void setAssemblyType(String assemblyType) {
        this.assemblyType = assemblyType;
    }

    public File getAssemblyPath() {
        return assemblyPath;
    }

    public void setAssemblyPath(File assemblyPath) {
        this.assemblyPath = assemblyPath;
    }

    public File getManifestPath() {
        return manifestPath;
    }

    public void setManifestPath(File manifestPath) {
        this.manifestPath = manifestPath;
    }

}
