package lv.ctco.scm.gradle.xamarin;

import lv.ctco.scm.mobile.utils.Profile;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XamarinConfigurationTest {

    @Test
    public void testGetSpecificProfiles() throws Exception {
        XamarinConfiguration configuration = new XamarinConfiguration();
        List<Profile> profiles = configuration.getSpecificProfiles("null", "null");
        assertTrue(profiles.isEmpty());
    }

}