package lv.ctco.scm.utils.exec;

public class CapturingOutputStream extends ExecOutputStream {

    public CapturingOutputStream() {
        this.setOutputFilter(new FullOutputFilter());
    }

    public CapturingOutputStream(ExecOutputFilter filter) {
        this.setOutputFilter(filter);
    }

}
