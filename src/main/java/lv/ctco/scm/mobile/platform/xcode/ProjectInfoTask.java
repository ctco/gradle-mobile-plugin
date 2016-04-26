/*
 * @(#)ProjectInfoTask.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xcode;

import lv.ctco.scm.mobile.core.utils.CommonUtil;
import lv.ctco.scm.mobile.core.utils.GitUtil;
import lv.ctco.scm.mobile.core.utils.LoggerUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;
import lv.ctco.scm.mobile.core.utils.RevisionUtil;
import lv.ctco.scm.mobile.core.utils.TeamcityUtil;

import org.apache.commons.lang3.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectInfoTask extends DefaultTask {

    private static final String DEFAULT_XCODE_VERSION = "0.1";

    private String libraryVersion;

    public void setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
    }

    @TaskAction
    public void doTaskAction() {
        try {
            Map<String, String> buildSettings = new XcodeUtil().getBuildSettings();
            String productType = buildSettings.get("PRODUCT_TYPE");
            if (StringUtils.isBlank(productType)) {
                LoggerUtil.info("Product Type is undefined");
            } else {
                LoggerUtil.info("Product Type: "+productType);
            }
            String version = DEFAULT_XCODE_VERSION;
            String versionLibrary = "";
            if ("com.apple.product-type.application".equalsIgnoreCase(productType)) {
                File infoPlist = new File(buildSettings.get("INFOPLIST_FILE"));
                version = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
                if (version == null) {
                    version = DEFAULT_XCODE_VERSION;
                    LoggerUtil.warn("Setting application release version as default Xcode version");
                } else {
                    LoggerUtil.info("Setting application release version from project's info plist");
                }
                LoggerUtil.lifecycle("Release Version: "+version);
                if (libraryVersion != null) {
                    if (libraryVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                        LoggerUtil.lifecycle("Stripping -SNAPSHOT marking");
                        versionLibrary = libraryVersion.substring(0, libraryVersion.length() - 9);
                        LoggerUtil.lifecycle("Library Version: " + versionLibrary);
                    } else {
                        versionLibrary = libraryVersion;
                        LoggerUtil.lifecycle("Library Version: " + versionLibrary);
                    }
                }
            } else {
                if (libraryVersion != null) {
                    LoggerUtil.lifecycle("Setting library release version as defined in ctcoMobile.xcode.libraryVersion");
                    if (libraryVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                        LoggerUtil.lifecycle("Stripping -SNAPSHOT marking");
                        version = libraryVersion.substring(0, libraryVersion.length() - 9);
                        versionLibrary = version;
                        LoggerUtil.lifecycle("Release Version: " + version);
                        LoggerUtil.lifecycle("Library Version: " + version);
                    } else {
                        version = libraryVersion;
                        versionLibrary = version;
                        LoggerUtil.lifecycle("Release Version: " + version);
                        LoggerUtil.lifecycle("Library Version: " + version);
                    }
                } else {
                    LoggerUtil.lifecycle("Setting release version as default value "+DEFAULT_XCODE_VERSION+" because it is undefined");
                    LoggerUtil.lifecycle("Release Version: "+DEFAULT_XCODE_VERSION);
                }
            }
            LoggerUtil.lifecycle("Project revision: "+RevisionUtil.getRevision());
            CommonUtil.printTeamcityInfo(version);
            printTcVersionLibrary(versionLibrary);
            LibraryUtil.printLibrariesPublicationsInfo(getProject());
            if (GitUtil.isGitDir(PathUtil.getProjectDir())) {
                GitUtil.generateCommitInfo();
            }
        } catch (IOException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void printTcVersionLibrary(String versionLibrary) throws IOException {
        String revision = RevisionUtil.getRevision();
        if (versionLibrary != null && !versionLibrary.isEmpty()) {
            TeamcityUtil.setAgentParameter("project.library.version.iteration", versionLibrary);
            setLibraryVersion(versionLibrary, revision);
        }
    }

    private void setLibraryVersion(String libraryVersion, String revision) {
        if(libraryVersion.length() != 0) {
            TeamcityUtil.setAgentParameter("project.library.version.publish", libraryVersion+"_"+revision);
        } else {
            String error = "Library version is empty!";
            LoggerUtil.errorInTask(this.getName(), error);
            throw new GradleException(error);
        }
    }

}
