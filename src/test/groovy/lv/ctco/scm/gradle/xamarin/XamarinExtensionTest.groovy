/*
 * @(#)XamarinExtensionTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xamarin

import lv.ctco.scm.mobile.utils.Profile
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class XamarinExtensionTest {

    @Ignore
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

    @Ignore
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

    @Ignore
    @Test
    void profileMandatoryFieldsSourcesTt() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', source: 'source.tt', target: 'target.txt'
        } catch (IOException e) {
            assertEquals e.getMessage(), "TextTransform profiling is no longer supported. Migrate to GroovyProfiling."
            return
        }
        fail('Should have thrown exception...')
    }

    @Ignore
    @Test
    void profileMandatoryFieldsTarget() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', source: 'source.txt'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Ignore
    @Test
    void profileExceptionFieldsTarget() {
        try {
            XamarinExtension extension = new XamarinExtension()
            extension.profile environment: 'TEST', source: 'source.groovy'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            fail('Should have NOT thrown exception...')
        }
    }

}
