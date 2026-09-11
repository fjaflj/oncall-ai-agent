package org.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiConfigTest {

    @Test
    void missingApiKeyFailsWithActionableMessage() {
        OpenAiConfig config = new OpenAiConfig();
        ReflectionTestUtils.setField(config, "apiKey", "");

        IllegalStateException error = assertThrows(IllegalStateException.class, config::validateApiKey);

        org.junit.jupiter.api.Assertions.assertEquals("OPENAI_API_KEY is required", error.getMessage());
    }

    @Test
    void configuredApiKeyPassesValidation() {
        OpenAiConfig config = new OpenAiConfig();
        ReflectionTestUtils.setField(config, "apiKey", "sk-test-only");

        assertDoesNotThrow(config::validateApiKey);
    }
}
