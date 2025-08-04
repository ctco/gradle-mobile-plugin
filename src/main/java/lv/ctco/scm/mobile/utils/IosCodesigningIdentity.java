/*
 * @(#)IosCodesigningIdentity.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import java.util.Arrays;
import java.util.List;

public class IosCodesigningIdentity {

    private String commonName;
    private String identityName;
    private String identityType;

    static final List<String> SUPPORTED_IDENTITY_TYPES = Arrays.asList(
            "Apple Distribution", "Apple Developer", "iPhone Distribution", "iPhone Developer"
    );

    public IosCodesigningIdentity() {}

    public IosCodesigningIdentity(String commonName) {
        this.commonName = commonName.trim();
        if (commonName.contains(":")) {
            String identityType = (commonName.trim().split(":")[0]).trim();
            if (SUPPORTED_IDENTITY_TYPES.contains(identityType)) {
                this.identityType = identityType;
                this.identityName = (commonName.split(":")[1]).trim();
            } else {
                throw new IllegalArgumentException("Unsupported commonName provided to IosCodesigningIdentity");
            }
        } else {
            throw new IllegalArgumentException("Invalid commonName provided to IosCodesigningIdentity");
        }
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
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

}
