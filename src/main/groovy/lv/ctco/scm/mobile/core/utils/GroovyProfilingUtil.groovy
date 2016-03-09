/*
 * @(#)GroovyProfilingUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import org.gradle.api.GradleException

@Singleton
class GroovyProfilingUtil {

    public static void profileUsingGroovyEval(File groovyFile) {
        if (!groovyFile.exists()) {
            throw new GradleException("Missing profiling file '"+groovyFile.getAbsolutePath()+"'")
        }
        def shell = new GroovyShell()
        def code = groovyFile.text
        code = "{->${code}}"
        def closure = shell.evaluate(code)
        closure.delegate = new GroovyProfilingEval()
        closure()
    }

}
