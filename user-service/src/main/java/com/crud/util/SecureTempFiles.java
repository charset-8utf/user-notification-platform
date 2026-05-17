package com.crud.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Временные файлы с правами только для владельца (Sonar java:S5443).
 */
public final class SecureTempFiles {

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_READ_WRITE =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));

    private SecureTempFiles() {
    }

    public static Path createTempFile(String prefix, String suffix) throws IOException {
        if (isPosix()) {
            return Files.createTempFile(prefix, suffix, OWNER_READ_WRITE);
        }
        Path temp = Files.createTempFile(prefix, suffix);
        restrictToOwnerOnly(temp.toFile());
        return temp;
    }

    private static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    private static void restrictToOwnerOnly(File file) throws IOException {
        requirePermission(file, "world-readable", file.setReadable(false, false));
        requirePermission(file, "world-writable", file.setWritable(false, false));
        requirePermission(file, "world-executable", file.setExecutable(false, false));
        requirePermission(file, "owner-readable", file.setReadable(true, true));
        requirePermission(file, "owner-writable", file.setWritable(true, true));
        requirePermission(file, "owner-executable", file.setExecutable(false, false));
    }

    private static void requirePermission(File file, String permission, boolean applied) throws IOException {
        if (!applied) {
            throw new IOException(
                    "Не удалось ограничить права доступа к временным файлам (" + permission + "): " + file.getAbsolutePath());
        }
    }
}
