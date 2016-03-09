/*
 * @(#)Profile.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

import java.util.Arrays;
import java.util.List;

public class Profile {

    private String environment;
    private String target;
    private String sources;
    private String[] scopes;
    private String[] additionalTargets;

    private static final String DEFAULT_SCOPE = "build";

    public Profile() {
        this.scopes = new String[]{DEFAULT_SCOPE};
    }

    public Profile(String environment, String target, String sources, String[] scopes, String[] additionalTargets) {
        this.environment = environment;
        this.target = target;
        this.sources = sources;
        this.scopes = scopes != null ? scopes : new String[]{DEFAULT_SCOPE};
        this.additionalTargets = additionalTargets;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        if (scopes != null) {
            this.scopes = scopes; // IMPROVEMENT : default if nulled?
        }
    }

    public void setScopes(List<String> scopes) {
        if (scopes != null) {
            this.scopes = scopes.toArray(new String[scopes.size()]); // IMPROVEMENT : default if nulled?
        }
    }

    public String[] getAdditionalTargets() {
        return additionalTargets;
    }

    public void setAdditionalTargets(String[] additionalTargets) {
        this.additionalTargets = additionalTargets;
    }

    public boolean hasScope(String scope) {
        return Arrays.asList(scopes).contains(scope);
    }

    private static String getArrayAsString(String[] array) {
        String result = null;
        for (String path : array) {
            if (result == null) {
                result = "'"+path+"'";
            } else {
                result = result+", '"+path+"'";
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Profile{" +
                " environment:\'"+environment+"\'"+
                (target == null ? "" : ", target:\'"+target+"\'")+
                ", sources:'" + sources + "'"+
                (scopes == null ? "" : ", scopes:["+getArrayAsString(scopes)+"]")+
                (additionalTargets == null ? "" : ",\n            additionalTargets:[" + getArrayAsString(additionalTargets) + ']') +
                " }";
    }

}
