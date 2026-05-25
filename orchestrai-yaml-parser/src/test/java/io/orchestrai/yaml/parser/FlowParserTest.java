package io.orchestrai.yaml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.orchestrai.core.enums.InputType;
import io.orchestrai.core.model.Flow;
import io.orchestrai.core.model.Task;
import io.orchestrai.yaml.validation.FlowValidator;
import io.orchestrai.yaml.validation.ValidationResult;

class FlowParserTest {

    private static final Path EXAMPLES = Path.of("..", "examples").toAbsolutePath().normalize();

    private FlowParser parser;
    private FlowValidator validator;

    @BeforeEach
    void setUp() {
        parser = new FlowParser();
        validator = new FlowValidator();
    }

    @Test
    void parse01HelloAgent() {
        Flow flow = parser.parse(EXAMPLES.resolve("01-hello-agent.yaml"));

        assertEquals("release-notes-copilot", flow.getFlowId());
        assertEquals("examples.getting-started", flow.getNamespace());
        assertEquals(2, flow.getInputs().size());
        assertEquals(InputType.STRING, flow.getInputs().get(0).getType());
        assertEquals(1, flow.getTasks().size());
        assertEquals("answer", flow.getTasks().get(0).getId());
        assertEquals("openai.chat", flow.getTasks().get(0).getType());
        assertNotNull(flow.getYamlSource());
        assertTrue(flow.getYamlSource().contains("release-notes-copilot"));

        assertTrue(validator.validate(flow).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "02-research-and-summarize.yaml",
            "03-content-moderation.yaml",
            "05-model-fallback.yaml",
            "15-churn-outreach-foreach.yaml"
    })
    void parseExampleFlows(String fileName) {
        Flow flow = parser.parse(EXAMPLES.resolve(fileName));

        assertNotNull(flow.getFlowId());
        assertFalse(flow.getFlowId().isBlank());
        assertNotNull(flow.getNamespace());
        assertFalse(flow.getTasks().isEmpty());

        ValidationResult result = validator.validate(flow);
        assertTrue(result.isValid(), () -> fileName + " validation errors: " + result.getErrors());
    }

    @Test
    void parseContentModeration_hasParallelAndIf() {
        Flow flow = parser.parse(EXAMPLES.resolve("03-content-moderation.yaml"));

        assertEquals("listing-moderation", flow.getFlowId());
        assertEquals("core.parallel", flow.getTasks().get(1).getType());
        assertEquals("core.if", flow.getTasks().get(2).getType());
        assertEquals(1, flow.getTasks().get(2).getThen().size());
        assertEquals(1, flow.getTasks().get(2).getElseTasks().size());
        assertFalse(flow.getOnFailure().isEmpty());
    }

    @Test
    void parseForeach_hasItemsExpression() {
        Flow flow = parser.parse(EXAMPLES.resolve("15-churn-outreach-foreach.yaml"));

        Task loop = flow.getTasks().stream()
                .filter(t -> "core.foreach".equals(t.getType()))
                .findFirst()
                .orElseThrow();
        assertEquals("{{ outputs.fetch-at-risk-accounts.body.accounts }}", loop.getItems());
        assertEquals(2, loop.getTasks().size());
    }

    @Test
    void parseModelFallback_hasRetryAndFallbackType() {
        Flow flow = parser.parse(EXAMPLES.resolve("05-model-fallback.yaml"));

        Task extract = flow.getTasks().get(0);
        assertNotNull(extract.getRetry());
        assertEquals(2, extract.getRetry().getMaxAttempts());
        assertNotNull(extract.getConfig().get("fallback"));
    }
}
