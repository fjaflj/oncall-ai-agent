package org.example.service;

import org.example.config.DocumentChunkConfig;
import org.example.dto.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkServiceTest {

    @Test
    void splitsMarkdownByHeadingAndKeepsTitleMetadata() {
        DocumentChunkConfig config = new DocumentChunkConfig();
        config.setMaxSize(800);
        config.setOverlap(100);
        DocumentChunkService service = new DocumentChunkService();
        ReflectionTestUtils.setField(service, "chunkConfig", config);

        List<DocumentChunk> chunks = service.chunkDocument("# CPU 告警\n\n检查节点负载。\n\n## 处理建议\n\n降低高负载任务。", "runbook.md");

        assertEquals(2, chunks.size());
        assertEquals("CPU 告警", chunks.get(0).getTitle());
        assertEquals("处理建议", chunks.get(1).getTitle());
    }

    @Test
    void returnsEmptyForBlankDocument() {
        DocumentChunkService service = new DocumentChunkService();
        DocumentChunkConfig config = new DocumentChunkConfig();
        ReflectionTestUtils.setField(service, "chunkConfig", config);

        assertTrue(service.chunkDocument("  ", "empty.md").isEmpty());
    }
}
