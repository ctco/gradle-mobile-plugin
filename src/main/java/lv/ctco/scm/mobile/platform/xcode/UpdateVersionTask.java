/*
 * @(#)UpdateVersionTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UpdateVersionTask extends DefaultTask {

    private static final String DEFAULT_VERSION_STRING = "0.1";
    private static final String PROP_VCS_ROOT_SUBS = "vcs.root.subs";

    private String targetName;

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            Map<String, String> buildSettings = new XcodeUtil().getBuildSettings(targetName);
            String productType = buildSettings.get("PRODUCT_TYPE");
            if ("com.apple.product-type.application".equalsIgnoreCase(productType)) {
                String revision = RevisionUtil.getRevision();
                File infoPlist = new File(buildSettings.get("INFOPLIST_FILE"));
                String releaseVersion = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
                if (StringUtils.isBlank(releaseVersion)) {
                    LoggerUtil.info("Release version not found in "+infoPlist.getName());
                    releaseVersion = DEFAULT_VERSION_STRING;
                } else {
                    LoggerUtil.info("Release version was found in "+infoPlist.getName());
                }
                LoggerUtil.info("Setting project release version as '"+releaseVersion+"'");
                String buildVersion;
                if (PropertyUtil.hasProjectProperty(PROP_VCS_ROOT_SUBS) && !PropertyUtil.getProjectProperty(PROP_VCS_ROOT_SUBS).isEmpty()) {
                    buildVersion = StringUtils.isBlank(releaseVersion) ? revision : releaseVersion+"."+revision;
                } else {
                    buildVersion = StringUtils.isBlank(releaseVersion) ? revision : releaseVersion+"_"+revision;
                }
                PlistUtil.setStringValue(infoPlist, "CFBundleVersion", buildVersion);
            } else {
                LoggerUtil.info("Product type is not 'application'. Skipping bundle version update as not needed.");
            }
        } catch (IOException e) {
            LoggerUtil.errorInTask(this.getName(), e.getMessage());
            throw new GradleException(e.getMessage(), e);
        }
    }

}
