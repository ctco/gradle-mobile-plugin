/*
 * @(#)IosProvisioningProfile.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.objects;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

public class IosProvisioningProfile {

    private String uuid;
    private String profileName;
    private String teamName;
    private long expirationDate;
    private String location;

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
        return new Date(expirationDate);
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate.getTime();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isExpired() {
        return getExpirationDate().before(new Date());
    }

    @Override
    public String toString() {
        return uuid+"|"+profileName+"|"+teamName+"|"+DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(expirationDate);
    }

}
