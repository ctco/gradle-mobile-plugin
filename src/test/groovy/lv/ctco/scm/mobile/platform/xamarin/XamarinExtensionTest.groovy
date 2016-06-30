/*
 * @(#)XamarinExtensionTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin

import lv.ctco.scm.mobile.core.objects.Profile

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class XamarinExtensionTest {

    @Test
    public void detectBaseName() {
        XamarinExtension extension = new XamarinExtension()
        extension.solutionFile = new File('/Users/guest/project/Project.sln')
        assertEquals extension.projectBaseName, 'Project'
        extension.solutionFile = new File('/Users/guest/projectN/ProjectN.sln')
        assertEquals extension.projectBaseName, 'Project'
    }

    @Test
    public void detectProjectAndAssemblyName() {
        XamarinExtension extension = new XamarinExtension()

        extension.projectBaseName = 'Project'
        assertEquals extension.projectName, 'Project.iOS'
        assertEquals extension.assemblyName, 'Project'

        extension.projectBaseName = 'ProjectN'
        assertEquals extension.projectName, 'Project.iOS'
        assertEquals extension.assemblyName, 'Project'
    }


    @Test
    void profileMandatoryFieldsEnvironment() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.addProfile(new Profile())
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile environment is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsSources() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile source is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsSourcesTt() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', sources: 'source.tt', target: 'target.txt'
        } catch (IOException ignore) {
            fail('Should have NOT thrown exception...')
        }
    }

    @Test
    void profileMandatoryFieldsTarget() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', sources: 'source.txt'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileExceptionFieldsTarget() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', sources: 'source.groovy'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            fail('Should have NOT thrown exception...')
        }
    }

}
