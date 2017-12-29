package lv.ctco.scm.mobile.utils;

public class IosSimulatorRuntime {

    private String identifier;
    private String name;

    IosSimulatorRuntime(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

}