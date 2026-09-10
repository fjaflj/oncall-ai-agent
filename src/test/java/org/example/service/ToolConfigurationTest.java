package org.example.service;

import org.example.agent.tool.QueryLogsTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.*;

class ToolConfigurationTest {
    @Test void missingMcpProviderReturnsEmptyTools() {
        assertEquals(0, new ChatService().getToolCallbacks().length);
    }

    @Test void mockLogsOnlyRegisteredWhenEnabled() {
        var runner = new ApplicationContextRunner().withUserConfiguration(QueryLogsTools.class);
        runner.withPropertyValues("cls.mock-enabled=true").run(ctx -> assertEquals(1, ctx.getBeansOfType(QueryLogsTools.class).size()));
        runner.withPropertyValues("cls.mock-enabled=false").run(ctx -> assertTrue(ctx.getBeansOfType(QueryLogsTools.class).isEmpty()));
    }
}
