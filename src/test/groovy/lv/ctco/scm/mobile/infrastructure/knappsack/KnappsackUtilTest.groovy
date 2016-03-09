/*
 * @(#)KnappsackUtilTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.infrastructure.knappsack

import org.gmock.GMockController

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

import static org.gmock.GMock.constructor

public class KnappsackUtilTest {

    /*

    def project
    GMockController gmc

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()
        gmc = new GMockController()
    }

    @Test
    void testValidateConfigurationUrlUndefined() {
        def mockExtension = gmc.mock(KnappsackExtension, constructor())
        mockExtension.url.returns('').stub()
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.message, 'Knappsack server URL configuration (convention: knappsack.url,' +
                        ' property: knappsack.url) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationUserNameUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.message, 'Knappsack user name configuration (convention: knappsack.userName,' +
                        ' property: knappsack.user.name) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationPasswordUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack user password configuration (convention: knappsack.password,' +
                        ' property: knappsack.user.password) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationApplicationIdUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack application ID configuration (convention: knappsack.applicationId,' +
                        ' property: knappsack.app.id) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationGroupIdUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack group ID configuration (convention: knappsack.groupId,' +
                        ' property: knappsack.group.id) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationStorageIdUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack storage ID configuration (convention: knappsack.storageId,' +
                        ' property: knappsack.storage.id) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationVersionUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('storageId').stub()
            version.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack application version configuration (convention: knappsack.version,' +
                        ' property: knappsack.version) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationWhatsNewUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('storageId').stub()
            version.returns('version').stub()
            whatsNew.returns('').stub()
            whatsNewFileName.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Recent changes for the version configuration (convention: knappsack.whatsNew,' +
                        ' property: knappsack.whatsnew or knappsack.whatsnew.file) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationWhatsNewBothDefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('storageId').stub()
            version.returns('version').stub()
            whatsNew.returns('New version 1.2.3').stub()
            whatsNewFileName.returns('whatsnew.html').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Properties knappsack.whatsNew and knappsack.whatsNewFileName" +\n' +
                        '                    " can not be used together'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationArtifactUndefined() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('storageId').stub()
            version.returns('version').stub()
            whatsNew.returns('New version 1.2.3').stub()
            whatsNewFileName.returns(null).stub()
            artifactFileName.returns('').stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'Knappsack artifact file name configuration (convention: knappsack.artifact,' +
                        ' property: knappsack.artifact) is not specified'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testValidateConfigurationKeyStoreWithoutPassword() {
        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url').stub()
            userName.returns('userName').stub()
            password.returns('password').stub()
            applicationId.returns('applicationId').stub()
            groupId.returns('groupId').stub()
            storageId.returns('storageId').stub()
            version.returns('version').stub()
            whatsNew.returns('New version 1.2.3').stub()
            whatsNewFileName.returns(null).stub()
            artifactFileName.returns('application-1.2.3.ipa').stub()
            keyStoreFileName.returns('cacerts').stub()
            keyStorePassword.returns(null).stub()
        }
        gmc.play {
            try {
                project.extensions.add('knappsack', new KnappsackExtension())
                KnappsackUtil.validateKnappsackExtension(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'When custom key store for Knappsack https connection is defined, it\'s password' +
                        ' should also be specified (convention: knappsack.keystorePassword,' +
                        ' property: knappsack.keystore.password'
                return
            }
            fail('GradleException was expected!')
        }
    }

    @Test
    void testLoadWhatsNewFile() {
        def mockWhatsNewFile = gmc.mock(new File('.')) {
            exists().returns(true)
            isFile().returns(true)
            text.returns('New version 1.2.3')
        }

        gmc.mock(KnappsackExtension, constructor()) {
            whatsNewFile.returns(mockWhatsNewFile).times(2)
            whatsNew.set('New version 1.2.3')
        }

        gmc.play {
            project.extensions.add('knappsack', new KnappsackExtension())
            KnappsackUtil.loadWhatsNewFile(project.knappsack)
        }
    }

    // TODO : Reenable
    /*
    @Test
    void testLoadWhatsNewFileDoesNotExist() {
        def mockWhatsNewFile = gmc.mock(new File('.')) {
            exists().returns(false)
            absolutePath.returns('/tmp/whatsnew.html')
        }

        def mockExtension = gmc.mock(KnappsackExtension, constructor())
        mockExtension.whatsNewFile.returns(mockWhatsNewFile).times(2)

        gmc.play {
            project.extensions.add("knappsack", new KnappsackExtension())
            try {
                KnappsackUtil.loadWhatsNewFile(project.knappsack)
            } catch (GradleException e) {
            assertEquals e.getMessage(), "What's new file /tmp/whatsnew.html does not exist"
            return
        }
        fail("GradleException was expected!")
        }
    }
    */

    // TODO : Reeanble
    /*
    @Test
    void testLoadWhatsNewFileNotRegular() {
        def mockWhatsNewFile = gmc.mock(new File('.')) {
            exists().returns(true)
            isFile().returns(false)
            absolutePath.returns('/tmp/whatsnew.html')
        }

        def mockExtension = gmc.mock(KnappsackExtension, constructor())
        mockExtension.whatsNewFile.returns(mockWhatsNewFile).times(2)

        gmc.play {
            project.extensions.add('knappsack', new KnappsackExtension())
            try {
                KnappsackUtil.loadWhatsNewFile(project.knappsack)
            } catch (GradleException e) {
                assertEquals e.getMessage(), 'What\'s new file /tmp/whatsnew.html is not a regular file'
                return
            }
            fail('GradleException was expected!')
        }
    }
    */

    /*
    @Test
    void testUploadArtifact() {
        def mockKeyStoreFile = gmc.mock(new File('cacerts'))
        mockKeyStoreFile.exists().returns(true)

        def mockArtifactFile = gmc.mock(new File(''))
        mockArtifactFile.absolutePath.returns('/home/user/app/application-1.2.3.ipa')

        gmc.mock(KnappsackExtension, constructor()) {
            url.returns('url')
            userName.returns('userName')
            password.returns('password')
            applicationId.returns('applicationId')
            groupId.returns('groupId')
            storageId.returns('storageId')
            version.returns('version')
            whatsNew.returns('New version 1.2.3')
            artifactFile.returns(mockArtifactFile)
            keyStoreFile.returns(mockKeyStoreFile)
            keyStorePassword.returns('qwerty')
        }
        gmc.mock(Knappsack, constructor('url')) {
            switchToExternalKeyStore(mockKeyStoreFile, 'qwerty')
            authenticate('userName', 'password')
            uploadArtifact(project.knappsack)
        }
        gmc.play {
            KnappsackExtension ext = new KnappsackExtension()
            project.extensions.add('knappsack', ext)
            //KnappsackUtil.setupKnappsackExtension(ext)
            KnappsackUtil.uploadArtifact(ext)
        }
    }
    */

}
