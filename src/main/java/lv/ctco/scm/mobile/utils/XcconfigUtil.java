package lv.ctco.scm.mobile.utils;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very limited utility for profiling of xcconfig files.
 * Allows for profiling of simple variable assignments containing and being delimited by as single "=".
 */
public class XcconfigUtil {

    private static final Logger logger = Logging.getLogger(XcconfigUtil.class);

    private static final Pattern simpleAssignmentPattern = Pattern.compile("^([a-zA-Z_]*)\\s*=\\s*([^=]*)$");

    private XcconfigUtil() {}

    public static void applyProfile(File sourceFile, File targetFile) throws IOException {
        logger.info("Profiling '{}'...", targetFile.getAbsolutePath());
        logger.debug("  initial md5={}", CommonUtil.getMD5Hex(targetFile));
        BackupUtil.backupFile(targetFile);
        validateSourceFile(sourceFile);
        Map<String, String> sourceEntries = getSourceEntries(sourceFile);
        List<String> targetLines = getLines(targetFile);
        List<String> profiledLines = new ArrayList<>();
        for (String targetLine : targetLines) {
            if (isSimpleAssignment(targetLine)) {
                Map.Entry<String, String> targetEntry = getSimpleAssignmentEntry(targetLine);
                if (sourceEntries.containsKey(targetEntry.getKey())) {
                    logger.info("  Replacing key '{}'", targetEntry.getKey());
                    profiledLines.add(targetEntry.getKey() + " = " + sourceEntries.get(targetEntry.getKey()));
                    sourceEntries.remove(targetEntry.getKey());
                } else {
                    profiledLines.add(targetLine);
                }
            } else {
                profiledLines.add(targetLine);
            }
        }
        for (Map.Entry<String, String> sourceEntry : sourceEntries.entrySet()) {
            logger.info("  Adding key '{}'", sourceEntry.getKey());
            profiledLines.add(sourceEntry.getKey() + " = " + sourceEntry.getValue());
        }
        Files.write(targetFile.toPath(), profiledLines, StandardCharsets.UTF_8);
        logger.debug("  current md5={}", CommonUtil.getMD5Hex(targetFile));
    }

    private static boolean isSimpleAssignment(String line) {
        return simpleAssignmentPattern.matcher(line.trim()).matches();
    }

    private static List<String> getLines(File file) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }

    private static Map.Entry<String, String> getSimpleAssignmentEntry(String line) {
        Matcher matcher = simpleAssignmentPattern.matcher(line.trim());
        matcher.matches();
        return new AbstractMap.SimpleImmutableEntry<>(matcher.group(1).trim(), matcher.group(2).trim());
    }

    private static Map<String, String> getSourceEntries(File sourceFile) throws FileNotFoundException {
        Map<String, String> entries = new HashMap<>();
        for (String line : getLines(sourceFile)) {
            Matcher matcher = simpleAssignmentPattern.matcher(line.trim());
            matcher.matches();
            entries.put(matcher.group(1).trim(), matcher.group(2).trim());
        }
        return entries;
    }

    /**
     * Enforces that the source file contains only simple variable assignments.
     */
    static void validateSourceFile(File sourceFile) throws IOException {
        for (String line : getLines(sourceFile)) {
            if (!isSimpleAssignment(line)) {
                throw new IOException("Unsupported profiling entity in '" + sourceFile.getAbsolutePath() + "'");
            }
        }
    }

}
