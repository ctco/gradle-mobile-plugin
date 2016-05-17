/*
 * @(#)Environment.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Environment {

    private String name;
    private String target;
    private String configuration;
    private String sdk;
    private File outputPath;

    public Environment() {
        // Public constructor for extension configuration.
    }

    public Environment(String name, String target) {
        this.name = name;
        this.target = target;
    }

    public Environment(String name, String configuration, File outputPath) {
        this.name = name;
        this.target = name;
        this.configuration = configuration;
        this.outputPath = outputPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (StringUtils.isBlank(target)) {
            target = name;
        }
    }

    public String getCamelName() {
        return StringUtils.capitalize(name.toLowerCase());
    }

    public String getUpperCaseName() {
        return StringUtils.upperCase(name);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public File getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String toString() {
        return "Environment{ " +
                "name:'"+name+"', " +
                "target:'"+target+"', " +
                "configuration:'"+configuration+"', " +
                "sdk:'"+sdk+"' " +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof Environment)) {
            return false;
        }
        Environment that = (Environment)obj;
        if (!StringUtils.equals(configuration, that.configuration)) {
            return false;
        }
        if (!StringUtils.equals(sdk, that.sdk)) {
            return false;
        }
        if (!StringUtils.equals(name, that.name)) {
            return false;
        }
        if (!outputPath.equals(that.outputPath)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        result = 31 * result + (sdk != null ? sdk.hashCode() : 0);
        result = 31 * result + (outputPath != null ? outputPath.hashCode() : 0);
        return result;
    }

}
