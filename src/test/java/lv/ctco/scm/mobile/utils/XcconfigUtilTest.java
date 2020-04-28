package lv.ctco.scm.mobile.utils;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class XcconfigUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() throws Exception {
        BackupUtil.restoreAllFiles();
    }

    private String getTrimmedFileContent(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8).trim();
    }

    private File initTemporaryFileFromResource(String resourceName) throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(resourceName);
        if (resource != null) {
            Path resourceFile = Paths.get(resource.toURI());
            Path temporaryFile = new File(tempFolder.getRoot(), resourceName).toPath();
            Files.createDirectories(temporaryFile.getParent());
            Files.copy(resourceFile, temporaryFile);
            return temporaryFile.toFile();
        } else {
            throw new IOException("Resource file '"+resourceName+"' was not found");
        }
    }

    @Test
    public void testAapplyProfile() {
        try {
            File sourceFile = initTemporaryFileFromResource("xcconfig/source.xcconfig");
            File targetFile = initTemporaryFileFromResource("xcconfig/target.xcconfig");
            File expectFile = initTemporaryFileFromResource("xcconfig/expect.xcconfig");
            XcconfigUtil.validateSourceFile(sourceFile);
            XcconfigUtil.applyProfile(sourceFile, targetFile);
            assertEquals(getTrimmedFileContent(expectFile), getTrimmedFileContent(targetFile));
        } catch (IOException | URISyntaxException e) {
            fail();
        }
    }

}
