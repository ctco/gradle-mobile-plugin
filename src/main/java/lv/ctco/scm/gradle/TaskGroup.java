/*
 * @(#)TaskGroup.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle;

public enum TaskGroup {

    UTILITY("Utility"),
    BUILD("Build"),
    TESTS("Test"),
    UPLOAD("Upload"),
    XDEPS("Xdeps");

    private String label;

    TaskGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
