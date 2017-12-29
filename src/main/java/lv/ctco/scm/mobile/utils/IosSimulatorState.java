package lv.ctco.scm.mobile.utils;

public enum IosSimulatorState {

    BOOTED("Booted"),
    BOOTING("Booting"),
    SHUTDOWN("Shutdown");

    private String state;

    IosSimulatorState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return state;
    }

}
