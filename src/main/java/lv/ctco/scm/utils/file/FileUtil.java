package lv.ctco.scm.utils.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    private FileUtil() {}

    public static void cleanDirectory(File target) throws IOException {
        cleanDirectory(target.toPath());
    }

    public static void cleanDirectory(Path target) throws IOException {
        if (!Files.isDirectory(target)) {
            throw new NotDirectoryException(target.toString());
        }
        List<Path> paths = Files.walk(target).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        paths.remove(target);
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

}
