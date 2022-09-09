package lv.ctco.scm.mobile.knappsack;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class KnappsackPublishingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().register("knappsackUpload", KnappsackUploadTask.class, new KnappsackUploadTaskConfigureAction());
    }

}
