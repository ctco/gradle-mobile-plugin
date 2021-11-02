package lv.ctco.scm.utils.exec;

public class LoggerOutputStream extends ExecOutputStream {

    public LoggerOutputStream() {
        this.setLoggerFilter(new FullOutputFilter());
    }

    public LoggerOutputStream(ExecOutputFilter filter) {
        this.setLoggerFilter(filter);
    }

}
