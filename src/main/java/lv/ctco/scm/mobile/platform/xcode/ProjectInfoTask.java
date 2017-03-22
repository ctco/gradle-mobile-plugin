/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.ErrorUtil;
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.PropertyUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;
import lv.ctco.scm.mobile.core.utils.StampUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectInfoTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ProjectInfoTask.class);

    private static final String DEFAULT_XCODE_VERSION = "0.1";

    private String libraryVersion;

    public void setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            Map<String, String> buildSettings = XcodeUtil.getBuildSettings();
            String productType = buildSettings.get("PRODUCT_TYPE");
            if (StringUtils.isBlank(productType)) {
                logger.info("Product Type is undefined");
            } else {
                logger.info("Product Type is {}", productType);
            }
            String releaseVersion = DEFAULT_XCODE_VERSION;
            String releaseVersionLibrary = "";
            if ("com.apple.product-type.application".equalsIgnoreCase(productType)) {
                File infoPlist = new File(buildSettings.get("INFOPLIST_FILE"));
                releaseVersion = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
                if (releaseVersion == null) {
                    releaseVersion = DEFAULT_XCODE_VERSION;
                    logger.warn("Setting application release version as default Xcode version");
                } else {
                    logger.info("Setting application release version from project's info plist");
                }
                printReleaseVersion(releaseVersion);
                if (libraryVersion != null) {
                    if (libraryVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                        logger.lifecycle("Stripping -SNAPSHOT marking");
                        releaseVersionLibrary = libraryVersion.substring(0, libraryVersion.length()-9);
                        printLibraryVersion(releaseVersionLibrary);
                    } else {
                        releaseVersionLibrary = libraryVersion;
                        printLibraryVersion(releaseVersionLibrary);
                    }
                }
            } else {
                if (libraryVersion != null) {
                    logger.info("Setting library release version as defined in ctcoMobile.xcode.libraryVersion");
                    if (libraryVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                        logger.lifecycle("Stripping -SNAPSHOT marking");
                        releaseVersion = libraryVersion.substring(0, libraryVersion.length()-9);
                        releaseVersionLibrary = releaseVersion;
                    } else {
                        releaseVersion = libraryVersion;
                        releaseVersionLibrary = releaseVersion;
                    }
                    printReleaseVersion(releaseVersion);
                    printLibraryVersion(releaseVersion);
                } else {
                    logger.lifecycle("Setting release version as default value "+DEFAULT_XCODE_VERSION+" because it is undefined");
                    printReleaseVersion(DEFAULT_XCODE_VERSION);
                }
            }
            String revision = RevisionUtil.getRevision(getProject());
            String buildVersion = releaseVersion+"."+revision;
            logger.lifecycle("Project's revision is '{}'", revision);
            logger.lifecycle("Project's build version is '{}'", buildVersion);

            TeamcityUtil.setBuildNumber(buildVersion);
            TeamcityUtil.setAgentParameter("build.number", buildVersion);
            TeamcityUtil.setAgentParameter("project.version.iteration", releaseVersion);
            if (PropertyUtil.hasProjectProperty(getProject(), "stamp")) {
                StampUtil.updateStamp(PropertyUtil.getProjectProperty(getProject(), "stamp"), releaseVersion);
            }

            printTcVersionLibrary(releaseVersionLibrary, revision);
            LibraryUtil.printLibrariesPublicationsInfo(getProject());

            GitUtil.generateCommitInfo(getProject());
        } catch (IOException e) {
            ErrorUtil.errorInTask(this.getName(), e);
        }
    }

    private void printReleaseVersion(String releaseVersion) {
        logger.lifecycle("Release version: "+releaseVersion);
    }

    private void printLibraryVersion(String releaseVersion) {
        logger.lifecycle("Library version: "+releaseVersion);
    }

    private void printTcVersionLibrary(String releaseVersionLibrary, String revision) {
        if (releaseVersionLibrary != null && !releaseVersionLibrary.isEmpty()) {
            TeamcityUtil.setAgentParameter("project.library.version.iteration", releaseVersionLibrary);
            setLibraryVersion(releaseVersionLibrary, revision);
        }
    }
    
    private void setLibraryVersion(String releaseVersionLibrary, String revision) {
        if(releaseVersionLibrary.length() != 0) {
            TeamcityUtil.setAgentParameter("project.library.version.publish", releaseVersionLibrary+"."+revision);
        } else {
            String error = "Library version is empty!";
            ErrorUtil.errorInTask(this.getName(), error);
        }
    }

}
