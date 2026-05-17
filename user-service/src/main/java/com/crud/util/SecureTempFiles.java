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
 * Временные файлы по рекомендациям Sonar java:S5443.
 */
public final class SecureTempFiles {

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_ONLY =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    private SecureTempFiles() {
    }

    public static Path createTempFile(String prefix, String suffix) throws IOException {
        if (isPosix()) {
            return Files.createTempFile(prefix, suffix, OWNER_ONLY);
        }
        File file = Files.createTempFile(prefix, suffix).toFile();
        requireApplied(file, "readable", file.setReadable(true, true));
        requireApplied(file, "writable", file.setWritable(true, true));
        requireApplied(file, "executable", file.setExecutable(true, true));
        return file.toPath();
    }

    private static void requireApplied(File file, String operation, boolean applied) throws IOException {
        if (!applied) {
            throw new IOException(
                    "Не удалось установить права (" + operation + ") для временного файла: " + file.getAbsolutePath());
        }
    }

    private static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}
