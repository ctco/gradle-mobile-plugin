/*
 * @(#)KnappsackExtensionTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.infrastructure.knappsack

import lv.ctco.scm.mobile.MobilePlugin
import lv.ctco.scm.mobile.core.utils.PropertyUtil

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

public class KnappsackExtensionTest {

    /*

    Project project

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()
        //project.apply(plugin: MobilePlugin)
        //project.getPlugins().findPlugin(MobilePlugin)
        PropertyUtil.setProject(project)
    }

    @Test
    void testBothWhatsNewParametersDefined() {
        KnappsackExtension ext = new KnappsackExtension()
        ext.whatsNew = "What's new"
        ext.whatsNewFileName = "/tmp/whatsnew.html"
        try {
            KnappsackUtil.setupKnappsackExtension(ext)
            KnappsackUtil.validateKnappsackExtension(ext)
        } catch (GradleException e) {
            assert(e.getMessage() != null)
            //assertEquals e.getMessage(), 'Properties knappsack.whatsnew and knappsack.whatsnew.file can not be used together'
            return
        }
        fail("Should have failed because two incompatible parameters have been defined...")
    }

    @Test
    void testReadConventionsFromPropertiesMostlyDefined() {
        project.ext['knappsack.url'] = 'http://server.com/path'
        project.ext['knappsack.user.name'] = 'userName'
        project.ext['knappsack.user.password'] = 'querty'
        project.ext['knappsack.app.id'] = '1'
        project.ext['knappsack.group.id'] = '2'
        project.ext['knappsack.storage.id'] = '3'
        project.ext['knappsack.version'] = '1.2.3'
        project.ext['knappsack.artifact'] = 'application.ipa'
        project.ext['knappsack.keystore.file'] = 'cacerts'
        project.ext['knappsack.keystore.password'] = 'azerty'

        MobilePlugin plugin = getAndApplyPlugin()
        //plugin.readConventionsFromProperties()
        KnappsackUtil.setupKnappsackExtension(project.knappsack)

        assertEquals project.knappsack.url, 'http://server.com/path'
        assertEquals project.knappsack.userName, 'userName'
        assertEquals project.knappsack.password, 'querty'
        assertEquals project.knappsack.applicationId, '1'
        assertEquals project.knappsack.groupId, '2'
        assertEquals project.knappsack.storageId, '3'
        assertEquals project.knappsack.version, '1.2.3'
        assertTrue project.knappsack.artifactFile.name.endsWith('application.ipa')
        assertTrue project.knappsack.keyStoreFile.name.endsWith('cacerts')
        assertEquals project.knappsack.keyStorePassword, 'azerty'
    }

    @Test
    void testReadConventionsFromPropertiesWhatsNewTest() {
        project.ext['knappsack.whatsnew'] = "1.2.3"

        MobilePlugin plugin = getAndApplyPlugin()
        //plugin.readConventionsFromProperties()
        KnappsackUtil.setupKnappsackExtension(project.knappsack)

        assertEquals("1.2.3", project.knappsack.whatsNew)
    }

    @Test
    void testReadConventionsFromPropertiesWhatsNewFileTest() {
        project.ext["knappsack.whatsnew.file"] = "whatsNew.html"

        MobilePlugin plugin = getAndApplyPlugin()
        KnappsackUtil.setupKnappsackExtension(project.knappsack)

        assertTrue project.knappsack.whatsNewFile.name.endsWith('whatsNew.html')
    }

    private MobilePlugin getAndApplyPlugin() {
        project.apply(plugin: MobilePlugin)
        project.getPlugins().findPlugin(MobilePlugin)
    }

    */

}
