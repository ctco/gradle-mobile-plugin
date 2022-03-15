package lv.ctco.scm.mobile.utils;

import lv.ctco.scm.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class IosArtifactUtil {

    private IosArtifactUtil() {}

    public static void unpackIpaPayload(File sourceIpa, File payloadContainerDir) throws IOException {
        if (payloadContainerDir.exists()) {
            FileUtil.cleanDirectory(payloadContainerDir);
        } else {
            Files.createDirectories(payloadContainerDir.toPath());
        }
        ZipUtil.extractAll(sourceIpa, payloadContainerDir);
    }

    public static void repackIpaPayload(File payloadContainerDir, File targetIpa) throws IOException {
        if (payloadContainerDir.exists()) {
            ZipUtil.compressDirectory(payloadContainerDir, false, targetIpa);
        } else {
            throw new IOException("Payload '"+payloadContainerDir.getAbsolutePath()+"' was not found!");
        }
    }

    public static File getPayloadApp(File payloadContainerDir) throws IOException {
        File payloadDir = new File(payloadContainerDir, "Payload");
        File[] files = payloadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".app")) {
                    return file;
                }
            }
        }
        throw new IOException(".app directory not found in '"+payloadContainerDir.getAbsolutePath()+"'");
    }

}
