/*
 * @(#)Environment.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import java.util.Objects;

public class Environment {

    private String name;
    private String target;
    private String configuration = "Release";
    private String sdk = "iphoneos";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "environment" +
                " name:'" + name + '\'' +
                ", target:'" + target + '\'' +
                ", configuration:'" + configuration + '\'' +
                ", sdk:'" + sdk + '\'';
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
                Objects.equals(target, that.target) &&
                Objects.equals(configuration, that.configuration) &&
                Objects.equals(sdk, that.sdk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, target, configuration, sdk);
    }

}
