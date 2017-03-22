/*
 * @(#)KnappsackExtension.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.infrastructure.knappsack;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class KnappsackExtension {

    /** Knappsack server URL. */
    private String url;

    /** Knappsack user name. */
    private String userName;

    /** Knappsack user password. */
    private String password;

    /** Knappsack application id. */
    private String applicationId;

    /** Knappsack group id. */
    private String groupId;

    /** Knappsack storage id. */
    private String storageId;

    /** Application version. */
    private String version;

    /** Recent changes description. */
    private String whatsNew;

    /** Name of file containing recent changes description. */
    private String whatsNewFileName;

    /** File name of the artifact to upload. */
    private String artifactFileName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }

    public String getWhatsNewFileName() {
        return whatsNewFileName;
    }

    public void setWhatsNewFileName(String whatsNewFileName) {
        this.whatsNewFileName = whatsNewFileName;
    }

    public File getWhatsNewFile() {
        return StringUtils.isBlank(whatsNewFileName) ? null : new File(whatsNewFileName);
    }

    public String getArtifactFileName() {
        return artifactFileName;
    }

    public void setArtifactFileName(String artifactFileName) {
        this.artifactFileName = artifactFileName;
    }

    public File getArtifactFile() {
        return StringUtils.isBlank(artifactFileName) ? null : new File(artifactFileName);
    }

}
