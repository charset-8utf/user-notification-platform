package com.crud.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTempFilesTest {

    @Test
    void createsTempFileWithContent() throws Exception {
        Path temp = SecureTempFiles.createTempFile("sonar-", ".tmp");
        try {
            assertThat(Files.exists(temp)).isTrue();
            assertThat(Files.isRegularFile(temp)).isTrue();
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
