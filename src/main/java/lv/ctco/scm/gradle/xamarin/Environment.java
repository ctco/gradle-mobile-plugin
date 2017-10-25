/*
 * @(#)Environment.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin;

import java.util.Objects;

public class Environment {

    private String name;
    private String configuration = "Release";
    private String platform;

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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("environment");
        sb.append(" name:'").append(name).append('\'');
        sb.append(", configuration:'").append(configuration).append('\'');
        if (platform != null) {
            sb.append(", platform:'").append(platform).append('\'');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Environment)) {
            return false;
        }
        Environment that = (Environment) object;
        return Objects.equals(name, that.name) &&
                Objects.equals(configuration, that.configuration) &&
                Objects.equals(platform, that.platform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configuration, platform);
    }

}
