package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VectorEmbeddingServiceTest {

    private EmbeddingModel embeddingModel;
    private VectorEmbeddingService service;

    @BeforeEach
    void setUp() {
        embeddingModel = mock(EmbeddingModel.class);
        service = new VectorEmbeddingService();
        ReflectionTestUtils.setField(service, "embeddingModel", embeddingModel);
    }

    @Test
    void generatesSingleVectorThroughSpringAiModel() {
        float[] vector = vector(1536, 0.25f);
        when(embeddingModel.embed(anyString())).thenReturn(vector);

        List<Float> result = service.generateEmbedding("CPU 告警排查");

        assertEquals(1536, result.size());
        assertEquals(0.25f, result.get(0));
        verify(embeddingModel).embed("CPU 告警排查");
    }

    @Test
    void generatesBatchVectorsInInputOrder() {
        List<float[]> vectors = List.of(vector(1536, 0.1f), vector(1536, 0.2f));
        when(embeddingModel.embed(anyList())).thenReturn(vectors);

        List<List<Float>> result = service.generateEmbeddings(List.of("a", "b"));

        assertEquals(2, result.size());
        assertEquals(0.1f, result.get(0).get(0));
        assertEquals(0.2f, result.get(1).get(0));
        verify(embeddingModel).embed(List.of("a", "b"));
    }

    @Test
    void rejectsVectorDimensionDifferentFromMilvusSchema() {
        when(embeddingModel.embed(anyString())).thenReturn(vector(3, 1.0f));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.generateEmbedding("dimension mismatch"));

        assertTrue(error.getMessage().contains("维度不匹配"));
    }

    private static float[] vector(int size, float value) {
        float[] vector = new float[size];
        java.util.Arrays.fill(vector, value);
        return vector;
    }
}
