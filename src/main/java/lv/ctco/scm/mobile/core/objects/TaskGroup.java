/*
 * @(#)TaskGroup.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

public enum TaskGroup {

    BUILD("Mobile Build"),
    UTILITY("Mobile Utility"),
    UTESTS("Mobile Unit Test"),
    UITESTS("Mobile UI Test"),
    PUBLISH("Mobile Artifact Publishing"),
    KNAPPSACK("Knappsack");

    private String label;

    private TaskGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
