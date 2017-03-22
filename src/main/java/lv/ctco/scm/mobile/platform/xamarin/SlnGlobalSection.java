/*
 * @(#)GlobalSection.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.platform.xamarin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Solution file section that contains project properties.
 */
public class SlnGlobalSection {

    private String name;

    Map<String, String> properties = new HashMap<>();

    SlnGlobalSection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    public boolean propertyExists(String key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SlnGlobalSection)) {
            return false;
        }
        SlnGlobalSection that = (SlnGlobalSection)obj;
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        if (!Objects.equals(properties, that.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = properties.hashCode();
        return 31 * result + name.hashCode();
    }

    @Override
    public String toString() {
        return name+" "+super.toString();
    }

}
