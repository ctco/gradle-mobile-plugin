/*
 * @(#)PosixUtil.java
 *
 * Copyright C.T.Co Ltd, 15/25 Jurkalnes Street, Riga LV-1046, Latvia. All rights reserved.
 */

package lv.ctco.scm.mobile.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public final class PosixUtil {

    private static final int O400 = 256;
    private static final int O200 = 128;
    private static final int O100 = 64;

    private static final int O040 = 32;
    private static final int O020 = 16;
    private static final int O010 = 8;

    private static final int O004 = 4;
    private static final int O002 = 2;
    private static final int O001 = 1;

    private PosixUtil() {}

    public static boolean isPosixFileStore(File file) throws IOException {
        return isPosixFileStore(file.toPath());
    }

    public static boolean isPosixFileStore(Path path) throws IOException {
        return Files.isSymbolicLink(path) || Files.getFileStore(path).supportsFileAttributeView("posix");
    }

    public static int getPosixPermissionsAsInt(Set<PosixFilePermission> permissionSet) {
        int result = 0;
        if (permissionSet.contains(PosixFilePermission.OWNER_READ)) {
            result = result | O400;
        }
        if (permissionSet.contains(PosixFilePermission.OWNER_WRITE)) {
            result = result | O200;
        }
        if (permissionSet.contains(PosixFilePermission.OWNER_EXECUTE)) {
            result = result | O100;
        }
        if (permissionSet.contains(PosixFilePermission.GROUP_READ)) {
            result = result | O040;
        }
        if (permissionSet.contains(PosixFilePermission.GROUP_WRITE)) {
            result = result | O020;
        }
        if (permissionSet.contains(PosixFilePermission.GROUP_EXECUTE)) {
            result = result | O010;
        }
        if (permissionSet.contains(PosixFilePermission.OTHERS_READ)) {
            result = result | O004;
        }
        if (permissionSet.contains(PosixFilePermission.OTHERS_WRITE)) {
            result = result | O002;
        }
        if (permissionSet.contains(PosixFilePermission.OTHERS_EXECUTE)) {
            result = result | O001;
        }
        return result;
    }

    public static Set<PosixFilePermission> getPosixPermissionsAsSet(int mode) {
        Set<PosixFilePermission> permissionSet = new HashSet<>();
        if ((mode & O400) == O400) {
            permissionSet.add(PosixFilePermission.OWNER_READ);
        }
        if ((mode & O200) == O200) {
            permissionSet.add(PosixFilePermission.OWNER_WRITE);
        }
        if ((mode & O100) == O100) {
            permissionSet.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if ((mode & O040) == O040) {
            permissionSet.add(PosixFilePermission.GROUP_READ);
        }
        if ((mode & O020) == O020) {
            permissionSet.add(PosixFilePermission.GROUP_WRITE);
        }
        if ((mode & O010) == O010) {
            permissionSet.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if ((mode & O004) == O004) {
            permissionSet.add(PosixFilePermission.OTHERS_READ);
        }
        if ((mode & O002) == O002) {
            permissionSet.add(PosixFilePermission.OTHERS_WRITE);
        }
        if ((mode & O001) == O001) {
            permissionSet.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return permissionSet;
    }

}
