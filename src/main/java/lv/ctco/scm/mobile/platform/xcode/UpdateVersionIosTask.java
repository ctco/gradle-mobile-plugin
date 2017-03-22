/*
 * @(#)UpdateVersionIosTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UpdateVersionIosTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(UpdateVersionIosTask.class);

    private static final String DEFAULT_VERSION_STRING = "0.1";

    private String targetName;

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            Map<String, String> buildSettings = XcodeUtil.getBuildSettings(targetName);
            String productType = buildSettings.get("PRODUCT_TYPE");
            if ("com.apple.product-type.application".equalsIgnoreCase(productType)) {
                String revision = RevisionUtil.getRevision(getProject());
                File manifestFile = new File(buildSettings.get("INFOPLIST_FILE"));
                String releaseVersion = PlistUtil.getStringValue(manifestFile, "CFBundleShortVersionString");
                if (StringUtils.isBlank(releaseVersion)) {
                    logger.info("Release version not found in '{}'", manifestFile.getName());
                    releaseVersion = DEFAULT_VERSION_STRING;
                } else {
                    logger.info("Release version was found in '{}'", manifestFile.getName());
                }
                logger.info("Setting project release version as '{}'", releaseVersion);
                String buildVersion = StringUtils.isBlank(releaseVersion) ? revision : releaseVersion+"."+revision;
                if (!manifestFile.exists()) {
                    throw new IOException("File not found");
                }
                PlistUtil.setStringValue(manifestFile, "CFBundleVersion", buildVersion);
            } else {
                logger.info("Product type is not 'application'. Skipping bundle version update as not needed.");
            }
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

}
