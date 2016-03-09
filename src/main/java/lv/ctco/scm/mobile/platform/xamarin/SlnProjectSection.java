/*
 * @(#)ProjectSection.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import java.io.File;
import java.util.Objects;

/**
 * A section of a solution file that lists all the projects in the solution and their properties.
 */
public class SlnProjectSection {

    private String type;
    private String name;
    private String uID;
    private String buildFilePath;

    SlnProjectSection(String name, String type, String buildFilePath, String uID) {
        this.name = name;
        this.type = type;
        this.uID = uID;
        this.buildFilePath = buildFilePath.replace("\\", File.separator);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getuID() {
        return uID;
    }

    public String getBuildFilePath() {
        return buildFilePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SlnProjectSection)) {
            return false;
        }
        SlnProjectSection that = (SlnProjectSection)obj;
        if (!Objects.equals(type, that.type)) {
            return false;
        }
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        if (!Objects.equals(uID, that.uID)) {
            return false;
        }
        if (!Objects.equals(buildFilePath, that.buildFilePath)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + uID.hashCode();
        result = 31 * result + buildFilePath.hashCode();
        return result;
    }

}
