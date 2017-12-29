package lv.ctco.scm.mobile.utils;

public final class IosSimulator {

    private String udid;
    private String name;
    private String type;
    private String runtime;

    IosSimulator(String udid, String name, String type, String runtime) {
        this.udid = udid;
        this.name = name;
        this.type = type.replace("com.apple.CoreSimulator.SimDeviceType.", "");
        this.runtime = runtime.replace("com.apple.CoreSimulator.SimRuntime.", "");
    }

    public String getUdid() {
        return udid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getSdkVersion() {
        return runtime.replace("iOS-", "").replace('-', '.');
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("iOS simulator {");
        sb.append(" udid:'").append(udid).append('\'');
        sb.append(", runtime:'").append(runtime).append('\'');
        sb.append(", type:'").append(type).append('\'');
        sb.append(" }");
        return sb.toString();
    }

}
