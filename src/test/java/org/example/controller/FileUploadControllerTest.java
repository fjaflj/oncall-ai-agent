package org.example.controller;

import org.example.config.FileUploadConfig;
import org.example.service.VectorIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileUploadControllerTest {
    @TempDir Path directory;
    FileUploadController controller;
    VectorIndexService index;

    @BeforeEach void setup() {
        controller = new FileUploadController();
        FileUploadConfig config = new FileUploadConfig();
        config.setPath(directory.resolve("uploads").toString());
        config.setAllowedExtensions("txt,md");
        index = mock(VectorIndexService.class);
        ReflectionTestUtils.setField(controller, "fileUploadConfig", config);
        ReflectionTestUtils.setField(controller, "vectorIndexService", index);
    }

    @ParameterizedTest
    @ValueSource(strings = {"../outside.md", "..\\outside.md", "/tmp/outside.md", "C:\\outside.md"})
    void rejectsPaths(String name) throws Exception {
        Path outside = directory.resolve("outside.md");
        Files.writeString(outside, "keep");
        var response = controller.upload(new MockMultipartFile("file", name, "text/plain", "attack".getBytes()));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("keep", Files.readString(outside));
        verifyNoInteractions(index);
    }

    @Test void successfulUploadIndexesSavedFile() throws Exception {
        var response = controller.upload(new MockMultipartFile("file", "runbook.md", "text/plain", "hello".getBytes()));
        Path saved = directory.resolve("uploads/runbook.md");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("hello", Files.readString(saved));
        verify(index).indexSingleFile(saved.toString());
    }

    @Test void indexFailureIsNotReportedAsSuccess() throws Exception {
        doThrow(new RuntimeException("unavailable")).when(index).indexSingleFile(anyString());
        var response = controller.upload(new MockMultipartFile("file", "runbook.md", "text/plain", "hello".getBytes()));
        assertEquals(503, response.getStatusCode().value());
        assertTrue(Files.exists(directory.resolve("uploads/runbook.md")));
    }

    @Test void rejectsUnsupportedAndEmptyFiles() {
        assertEquals(400, controller.upload(new MockMultipartFile("file", "bad.exe", "text/plain", "hello".getBytes())).getStatusCode().value());
        assertEquals(400, controller.upload(new MockMultipartFile("file", "empty.md", "text/plain", new byte[0])).getStatusCode().value());
        verifyNoInteractions(index);
    }
}
