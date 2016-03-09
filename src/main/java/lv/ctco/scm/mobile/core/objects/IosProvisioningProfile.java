/*
 * @(#)IosProvisioningProfile.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

import lv.ctco.scm.mobile.core.utils.IosProvisioningUtil;
import lv.ctco.scm.mobile.core.utils.PathUtil;
import lv.ctco.scm.mobile.core.utils.PlistUtil;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class IosProvisioningProfile {

    private String uuid;
    private String profileName;
    private String teamName;
    private Date expirationDate;
    private String location;

    public IosProvisioningProfile(File profileFile) throws IOException {
        File plist = new File(PathUtil.getTempDir(), profileFile.getName()+".plist");
        IosProvisioningUtil.convertProvisioningToPlist(profileFile, plist);
        this.uuid = PlistUtil.getStringValue(plist, "UUID");
        this.profileName = PlistUtil.getStringValue(plist, "Name");
        this.teamName = PlistUtil.getStringValue(plist, "TeamName");
        this.expirationDate = PlistUtil.getDateValue(plist, "ExpirationDate");
        this.location = profileFile.getAbsolutePath();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isExpired() {
        return expirationDate.before(new Date());
    }

    @Override
    public String toString() {
        return uuid+"|"+profileName+"|"+teamName+"|"+DateFormatUtils.ISO_DATETIME_FORMAT.format(expirationDate);
    }

}
