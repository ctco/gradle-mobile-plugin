/*
 * @(#)XcodeExtensionTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.gradle.xcode

import lv.ctco.scm.gradle.xamarin.Environment
import lv.ctco.scm.mobile.utils.Profile
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

// TODO : Refactor tests after plugin refactoring
@Ignore
class XcodeExtensionTest {

    @Test
    void environmentMandatoryFieldsName() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.addEnvironment(new Environment())
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Environment name is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Ignore
    @Test
    void environmentAlreadyDefined() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.environment name: 'test', target: 'test'
            extension.environment name: 'test', target: 'test'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Environment test is already defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Ignore
    @Test
    void environmentAlreadyDefinedCaseInsensitive() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.environment name: 'TEST', target: 'test'
            extension.environment name: 'test', target: 'test'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Environment test is already defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsEnvironment() {
        try {
            XcodeConfiguration.addProfile(new Profile())
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile environment is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsSources() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.profile environment: 'TEST'
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile source is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileExceptionFieldsSources() {
        try {
            Profile profile = new Profile()
            profile.setEnvironment("TEST")
            profile.setSource("source.tt")
            XcodeConfiguration.addProfile(profile)
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile source *.tt is not supported for Xcode'
            return
        }
        fail('Should have thrown exception...')
    }

    @Ignore
    @Test
    void profileMandatoryFieldsTarget() {
        try {
            Profile profile = new Profile()
            profile.setEnvironment("TEST")
            profile.setSource("source.txt")
            XcodeConfiguration.addProfile(profile)
        } catch (IOException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileExceptionFieldsTarget() {
        try {
            Profile profile = new Profile()
            profile.setEnvironment("TEST")
            profile.setSource("source.groovy")
            XcodeConfiguration.addProfile(profile)
        } catch (IOException ignore) {
            fail('Should have NOT thrown exception...')
        }
    }

}
