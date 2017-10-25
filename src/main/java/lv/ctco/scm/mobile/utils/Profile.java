/*
 * @(#)Profile.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

public class Profile implements Comparable<Profile> {

    private String environment;
    private String scope;
    private String source;
    private String target;
    private Integer order;
    private Integer level;

    public Profile() {
        this.scope = "build";
        this.order = 1;
        this.level = 1;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("profile");
        sb.append(" environment:'").append(environment).append('\'');
        sb.append(", scope:'").append(scope).append('\'');
        if (target != null) {
            sb.append(", target:'").append(target).append('\'');
        }
        sb.append(", order:'").append(order).append('\'');
        sb.append(", level:'").append(level).append('\'');
        sb.append(", source:'").append(source).append('\'');
        return sb.toString();
    }

    @Override
    public int compareTo(Profile that) {
        int i = ObjectUtils.compare(environment, that.environment);
        if (i != 0) {
            return i;
        }
        i = ObjectUtils.compare(scope, that.scope);
        if (i != 0) {
            return i;
        }
        i = ObjectUtils.compare(target, that.target);
        if (i != 0) {
            return i;
        }
        i = ObjectUtils.compare(order, that.order);
        if (i != 0) {
            return i;
        }
        i = ObjectUtils.compare(level, that.level);
        if (i != 0) {
            return i;
        }
        return ObjectUtils.compare(source, that.source);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Profile)) {
            return false;
        }
        Profile that = (Profile) object;
        return Objects.equals(environment, that.environment) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(target, that.target) &&
                Objects.equals(order, that.order) &&
                Objects.equals(level, that.level) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, scope, target, order, level, source);
    }

}
