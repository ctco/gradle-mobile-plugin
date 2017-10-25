package lv.ctco.scm.mobile.utils;

public enum IosSimulatorCLP {

    UDID("ios.simulator.udid"),
    TYPE("ios.simulator.type"),
    RUNTIME("ios.simulator.runtime"),
    SCALE("ios.simulator.scale");

    private String name;

    IosSimulatorCLP(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
