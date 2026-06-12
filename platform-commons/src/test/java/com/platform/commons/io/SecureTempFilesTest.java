package com.platform.commons.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTempFilesTest {

    @Test
    void createsTempFile() throws Exception {
        Path temp = SecureTempFiles.createTempFile("jacoco-", ".tmp");
        try {
            assertThat(Files.exists(temp)).isTrue();
            assertThat(Files.isRegularFile(temp)).isTrue();
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void createsTempFileWithOwnerOnlyPermissionsOnPosix() throws Exception {
        if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            return;
        }
        Path temp = SecureTempFiles.createTempFile("posix-", ".tmp");
        try {
            var permissions = Files.getPosixFilePermissions(temp);
            assertThat(permissions)
                    .containsOnly(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE
                    );
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
