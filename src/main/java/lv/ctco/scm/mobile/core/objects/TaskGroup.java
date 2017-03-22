/*
 * @(#)TaskGroup.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

public enum TaskGroup {

    UTILITY("Utility"),
    BUILD("Build"),
    TESTS("Test"),
    UPLOAD("Upload"),
    PUBLISH("Publish");

    private String label;

    private TaskGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
