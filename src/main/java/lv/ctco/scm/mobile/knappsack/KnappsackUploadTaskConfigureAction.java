package lv.ctco.scm.mobile.knappsack;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;

public class KnappsackUploadTaskConfigureAction implements Action<Task> {

    @Override
    public void execute(Task task) {
        if (task instanceof KnappsackUploadTask) {
            KnappsackUploadTask knappsackUploadTask = (KnappsackUploadTask) task;
            knappsackUploadTask.setExtension(KnappsackUtil.setupKnappsackExtension(task.getProject()));
        } else {
            throw new GradleException(this.getClass().getSimpleName()+" can not be applied to "+task.getClass().getSimpleName());
        }
    }

}
