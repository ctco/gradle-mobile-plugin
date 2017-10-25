package lv.ctco.scm.mobile.utils;

public final class IosSimulator {

    private String udid;
    private String type;
    private String runtime;

    public IosSimulator(String udid, String name, String runtime) {
        this.udid = udid;
        this.type = convertToIdentifier(name);
        this.runtime = convertToIdentifier(runtime);
    }

    public String getUdid() {
        return udid;
    }

    public String getType() {
        return type;
    }

    public String getRuntime() {
        return runtime;
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

    private String convertToIdentifier(String identifier) {
        return identifier.replace(" - ", "-")
                .replace(' ', '-')
                .replace('.', '-')
                .replace('(', '-')
                .replace(')', '-');
    }

}
