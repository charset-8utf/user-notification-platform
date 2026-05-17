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
 * <p>
 * Unix: {@link Files#createTempFile(String, String, FileAttribute[])} с {@code rwx------}.
 * Windows: {@link File#createTempFile(String, String, File)} в каталоге только для владельца.
 */
public final class SecureTempFiles {

    private static final FileAttribute<Set<PosixFilePermission>> OWNER_ONLY =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    private static Path secureDirectory;

    private SecureTempFiles() {
    }

    public static Path createTempFile(String prefix, String suffix) throws IOException {
        if (isPosix()) {
            return Files.createTempFile(prefix, suffix, OWNER_ONLY);
        }
        File file = File.createTempFile(prefix, suffix, secureDirectory().toFile());
        requireApplied(file, "readable", file.setReadable(true, true));
        requireApplied(file, "writable", file.setWritable(true, true));
        requireApplied(file, "executable", file.setExecutable(true, true));
        return file.toPath();
    }

    private static synchronized Path secureDirectory() throws IOException {
        if (secureDirectory == null) {
            secureDirectory = initSecureDirectory();
        }
        return secureDirectory;
    }

    private static Path initSecureDirectory() throws IOException {
        Path directory = Path.of(System.getProperty("user.home"), ".user-service", "tmp");
        Files.createDirectories(directory);
        File dir = directory.toFile();
        requireApplied(dir, "world-readable", dir.setReadable(false, false));
        requireApplied(dir, "world-writable", dir.setWritable(false, false));
        requireApplied(dir, "world-executable", dir.setExecutable(false, false));
        requireApplied(dir, "owner-readable", dir.setReadable(true, true));
        requireApplied(dir, "owner-writable", dir.setWritable(true, true));
        requireApplied(dir, "owner-executable", dir.setExecutable(true, true));
        return directory;
    }

    private static void requireApplied(File file, String operation, boolean applied) throws IOException {
        if (!applied) {
            throw new IOException(
                    "Не удалось установить права (" + operation + ") для: " + file.getAbsolutePath());
        }
    }

    private static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}
