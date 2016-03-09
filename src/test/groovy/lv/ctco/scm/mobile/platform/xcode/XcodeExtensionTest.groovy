/*
 * @(#)XcodeExtensionTest.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode

import lv.ctco.scm.mobile.core.objects.Environment
import lv.ctco.scm.mobile.core.objects.Profile

import org.gradle.api.GradleException

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class XcodeExtensionTest {

    @Test
    void environmentMandatoryFieldsName() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.addEnvironment(new Environment())
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Environment name is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void environmentAlreadyDefined() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.environment name: 'test', target: 'test'
            extension.environment name: 'test', target: 'test'
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Environment test is already defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void environmentAlreadyDefinedCaseInsensitive() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.environment name: 'TEST', target: 'test'
            extension.environment name: 'test', target: 'test'
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Environment test is already defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsEnvironment() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.addProfile(new Profile())
        } catch (GradleException e) {
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
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Profile source is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileExceptionFieldsSources() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.profile environment: 'TEST', sources: 'source.tt'
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Profile source *.tt is not supported for Xcode'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileMandatoryFieldsTarget() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.profile environment: 'TEST', sources: 'source.txt'
        } catch (GradleException e) {
            assertEquals e.getMessage(), 'Profile target is not defined'
            return
        }
        fail('Should have thrown exception...')
    }

    @Test
    void profileExceptionFieldsTarget() {
        try {
            XcodeExtension extension = new XcodeExtension()
            extension.profile environment: 'TEST', sources: 'source.groovy'
        } catch (GradleException ignore) {
            fail('Should have NOT thrown exception...')
        }
    }

}
