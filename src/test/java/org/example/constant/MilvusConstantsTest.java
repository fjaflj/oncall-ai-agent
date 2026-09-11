package org.example.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MilvusConstantsTest {
    @Test
    void usesDedicatedOpenAiCollectionAndEmbeddingDimension() {
        assertEquals("oncall_knowledge_openai", MilvusConstants.MILVUS_COLLECTION_NAME);
        assertEquals(1536, MilvusConstants.VECTOR_DIM);
    }
}
