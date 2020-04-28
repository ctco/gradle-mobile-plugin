package lv.ctco.scm.wercker;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

public class SetupConfigFileTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(SetupConfigFileTask.class);

    private String envVar;
    private String filePath;

    public SetupConfigFileTask() {
        this.setGroup("Wercker");
    }

    public void setEnvVar(String envVar) {
        this.envVar = envVar;
    }

    public void setFilePath(String filePath) {
        if (filePath.contains("~")) {
            this.filePath = filePath.replace("~", System.getProperty("user.home"));
        } else {
            this.filePath = filePath;
        }
    }

    @TaskAction
    public void executeTaskAction() {
        if (envVar == null) {
            throw new GradleException("The name of the environment variable (envVar) was not defined");
        }
        if (filePath == null) {
            throw new GradleException("The file path (filePath) was not defined");
        }
        String encodedValue = System.getenv().get(envVar);
        if (encodedValue == null) {
            throw new GradleException("The required environment variable was not provided");
        }
        String decodedValue = new String(Base64.getDecoder().decode(encodedValue));
        logger.lifecycle("Setting up content of '${}' to '{}'", envVar, filePath);
        try (FileWriter fileWriter = new FileWriter(new File(filePath))) {
            fileWriter.append(decodedValue);
        } catch (IOException e) {
            throw new GradleException("Could not write to the required file path", e);
        }
    }

}
