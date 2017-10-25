/*
 * @(#)IosApp.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class IosApp {

    private String name;
    private String buildCnf;
    private String buildSdk;

    private String identityName;
    private String identityType;

    private String provisioningUuid;
    private String provisioningProfileName;
    private String provisioningTeamName;
    private long provisioningExpiration;

    private String bundleName;
    private String bundleIdentifier;
    private String bundleVersion;
    private String bundleVersionShort;

    public IosApp(File appDir) throws IOException {
        getBundleInfo(appDir);
        getCodesigningIdentity(appDir);
        getCodesigningProvisioning(appDir);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuildCnf() {
        return buildCnf;
    }

    public void setBuildCnf(String buildCnf) {
        this.buildCnf = buildCnf;
    }

    public String getBuildSdk() {
        return buildSdk;
    }

    public void setBuildSdk(String buildSdk) {
        this.buildSdk = buildSdk;
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public boolean isSignedWithDistributionIdentity() {
        return "iPhone Distribution".equals(identityType);
    }

    public boolean isSignedWithDeveloperIdentity() {
        return "iPhone Developer".equals(identityType);
    }

    public String getProvisioningUuid() {
        return provisioningUuid;
    }

    public void setProvisioningUuid(String provisioningUuid) {
        this.provisioningUuid = provisioningUuid;
    }

    public String getProvisioningProfileName() {
        return provisioningProfileName;
    }

    public void setProvisioningProfileName(String provisioningProfileName) {
        this.provisioningProfileName = provisioningProfileName;
    }

    public String getProvisioningTeamName() {
        return provisioningTeamName;
    }

    public void setProvisioningTeamName(String provisioningTeamName) {
        this.provisioningTeamName = provisioningTeamName;
    }

    public Date getProvisioningExpiration() {
        return new Date(provisioningExpiration);
    }

    public void setProvisioningExpiration(Date provisioningExpiration) {
        this.provisioningExpiration = provisioningExpiration.getTime();
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    public void setBundleIdentifier(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    public String getBundleVersionShort() {
        return bundleVersionShort;
    }

    public void setBundleVersionShort(String bundleVersionShort) {
        this.bundleVersionShort = bundleVersionShort;
    }

    private void getBundleInfo(File appDir) throws IOException {
        File infoPlist = new File(appDir, "Info.plist");
        this.bundleName = PlistUtil.getStringValue(infoPlist, "CFBundleName");
        this.bundleIdentifier = PlistUtil.getStringValue(infoPlist, "CFBundleIdentifier");
        this.bundleVersion = PlistUtil.getStringValue(infoPlist, "CFBundleVersion");
        this.bundleVersionShort = PlistUtil.getStringValue(infoPlist, "CFBundleShortVersionString");
    }

    private void getCodesigningIdentity(File appDir) {
        IosCodesigningIdentity identity = IosCodesigningUtil.getCodesigningIdentity(appDir);
        if (identity != null) {
            this.identityName = identity.getIdentityName();
            this.identityType = identity.getIdentityType();
        }
    }

    private void getCodesigningProvisioning(File appDir) throws IOException {
        IosProvisioningProfile profile = IosCodesigningUtil.getEmbeddedProvisioningProfile(appDir);
        if (profile != null) {
            this.provisioningUuid = profile.getUuid();
            this.provisioningProfileName = profile.getProfileName();
            this.provisioningTeamName = profile.getTeamName();
            this.provisioningExpiration = profile.getExpirationDate().getTime();
        }
    }

}
