/*
 * @(#)XdepsExtension.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xdeps

import groovy.transform.TypeChecked

@TypeChecked
class XdepsExtension {

    String groupId
    String version

    XdepsConfiguration getXdepsConfiguration() {
        XdepsConfiguration configuration = new XdepsConfiguration()
        configuration.setGroupId(groupId)
        configuration.setVersion(version)
        return configuration
    }

}
