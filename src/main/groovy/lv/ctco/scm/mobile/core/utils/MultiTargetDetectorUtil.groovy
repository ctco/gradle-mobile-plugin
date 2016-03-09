/*
 * @(#)MultiTargetDetectorUtil.groovy
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Determines if project's targets correspond to a multi-target naming convention and extracts target names.
 */
class MultiTargetDetectorUtil {

    private String postfix = ""

    MultiTargetDetectorUtil() {
    }

    MultiTargetDetectorUtil(String postfix) {
        this.postfix = postfix
    }

    /**
     * Tries to determine is project's targets follow the multi-configuration naming rule <app name> <environment name>
     *     e.g. Cabinet DEV, Cabinet TRAIN, Cabinet UAT, etc...
     *
     * The following process takes place:
     * 1. Does the defaultTarget name matches the general naming rule: a word, then space, then another word
     * 2. If it does not then the project is considered to be a single-configuration project.
     * 3. If it does then we find all targets that match <the first word><space><any word> pattern
     * 4. All configuration names that correspond to this pattern are considered to belong to a certain environments, which
     *      name is the second word.
     *
     * @param defaultTarget The default configuration name.
     * @param targets A list of all project targets.
     * @return A map of environment names and corresponding configuration names that are considered to be multi-configuration.
     */
    public HashMap<String, String> detectEnvironmentTargets(String defaultTarget, List<String> targets) {
        Matcher m = Pattern.compile("(\\w+) \\w+$postfix").matcher(defaultTarget)
        if (m.matches()) {
            String environmentTargetPrefix = m.group(1)
            LoggerUtil.debug("Possible multi-target project found because the default target name" +
                    " '$defaultTarget' matches the multi-target project naming convention")
            return detectEnvironmentTargetsWithPrefix(environmentTargetPrefix, targets)
        } else {
            return new HashMap<String, String>()
        }

    }

    public HashMap<String, String> detectEnvironmentTargetsWithPrefix(String prefix, List<String> targets) {
        Pattern namePattern = Pattern.compile("${Pattern.quote(prefix)} (\\w+)${Pattern.quote(postfix)}")
        Map<String, String> environments = new HashMap<String, String>() // Environment name, configuration name
        targets.each {
            Matcher matcher = namePattern.matcher(it)
            if (matcher.matches()) {
                environments.put(matcher.group(1), it)
            }
        }
        return environments
    }

}
