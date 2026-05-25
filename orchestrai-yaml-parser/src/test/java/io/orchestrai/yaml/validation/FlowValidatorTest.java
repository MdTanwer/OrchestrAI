package io.orchestrai.yaml.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.orchestrai.core.model.Flow;
import io.orchestrai.core.model.Input;
import io.orchestrai.core.model.Task;
import io.orchestrai.yaml.parser.FlowParser;

class FlowValidatorTest {

    private FlowValidator validator;
    private FlowParser parser;

    @BeforeEach
    void setUp() {
        validator = new FlowValidator();
        parser = new FlowParser();
    }

    @Test
    void validMinimalFlow_passes() {
        Flow flow = Flow.builder()
                .flowId("demo-flow")
                .namespace("examples.test")
                .tasks(List.of(Task.builder().id("t1").type("core.log").build()))
                .build();

        ValidationResult result = validator.validate(flow);
        assertTrue(result.isValid());
    }

    @Test
    void missingFlowId_fails() {
        Flow flow = Flow.builder()
                .namespace("examples.test")
                .tasks(List.of(Task.builder().id("t1").type("core.log").build()))
                .build();

        ValidationResult result = validator.validate(flow);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("id")));
    }

    @Test
    void invalidFlowIdPattern_fails() {
        Flow flow = Flow.builder()
                .flowId("Invalid_Flow_ID")
                .namespace("examples.test")
                .tasks(List.of(Task.builder().id("t1").type("core.log").build()))
                .build();

        assertFalse(validator.validate(flow).isValid());
    }

    @Test
    void emptyTasks_fails() {
        Flow flow = Flow.builder()
                .flowId("demo")
                .namespace("examples.test")
                .tasks(List.of())
                .build();

        assertFalse(validator.validate(flow).isValid());
    }

    @Test
    void duplicateTaskIdsInSameBlock_fails() {
        Flow flow = Flow.builder()
                .flowId("demo")
                .namespace("examples.test")
                .tasks(List.of(
                        Task.builder().id("step").type("core.log").build(),
                        Task.builder().id("step").type("core.log").build()))
                .build();

        ValidationResult result = validator.validate(flow);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("duplicate task id")));
    }

    @Test
    void unknownTaskType_fails() {
        Flow flow = Flow.builder()
                .flowId("demo")
                .namespace("examples.test")
                .tasks(List.of(Task.builder().id("t1").type("acme.unknown").build()))
                .build();

        ValidationResult result = validator.validate(flow);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("unknown task type")));
    }

    @Test
    void coreIfMissingCondition_fails() {
        Flow flow = Flow.builder()
                .flowId("demo")
                .namespace("examples.test")
                .tasks(List.of(Task.builder()
                        .id("gate")
                        .type("core.if")
                        .then(List.of(Task.builder().id("t").type("core.log").build()))
                        .build()))
                .build();

        ValidationResult result = validator.validate(flow);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("condition")));
    }

    @Test
    void duplicateInputIds_fails() {
        Flow flow = Flow.builder()
                .flowId("demo")
                .namespace("examples.test")
                .inputs(List.of(
                        Input.builder().id("x").type(io.orchestrai.core.enums.InputType.STRING).build(),
                        Input.builder().id("x").type(io.orchestrai.core.enums.InputType.STRING).build()))
                .tasks(List.of(Task.builder().id("t1").type("core.log").build()))
                .build();

        assertFalse(validator.validate(flow).isValid());
    }

    @Test
    void parsedInvalidYamlFlow_failsValidation() {
        String yaml = """
                id: bad-demo
                namespace: examples.test
                tasks:
                  - id: only
                    type: not.a.real.plugin
                    message: hi
                """;

        Flow flow = parser.parse(yaml);
        ValidationResult result = validator.validate(flow);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().stream().filter(e -> e.contains("unknown task type")).count());
    }
}
