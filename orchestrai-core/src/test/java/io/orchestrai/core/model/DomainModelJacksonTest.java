package io.orchestrai.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.orchestrai.core.enums.BackoffType;
import io.orchestrai.core.enums.ExecutionState;
import io.orchestrai.core.enums.InputType;
import io.orchestrai.core.enums.LogLevel;
import io.orchestrai.core.enums.TaskRunState;
import io.orchestrai.core.enums.TriggerType;
import io.orchestrai.core.jackson.ObjectMappers;

class DomainModelJacksonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = ObjectMappers.json();
    }

    @Test
    void flow_roundTrip_json() throws Exception {
        Flow original = sampleFlow();
        String json = mapper.writeValueAsString(original);
        Flow restored = mapper.readValue(json, Flow.class);

        assertEquals(original.getFlowId(), restored.getFlowId());
        assertEquals(original.getNamespace(), restored.getNamespace());
        assertEquals(original.getInputs().get(0).getType(), restored.getInputs().get(0).getType());
        assertEquals("openai.chat", restored.getTasks().get(0).getType());
        assertEquals("gpt-4o-mini", restored.getTasks().get(0).getConfig().get("model"));
        assertEquals("0 0 * * *", restored.getTriggers().get(0).getConfig().get("cron"));
    }

    @Test
    void flow_deserialize_fromYaml() throws Exception {
        String yaml = """
                id: release-notes-copilot
                namespace: examples.getting-started
                description: Answer from release notes
                inputs:
                  - id: question
                    type: STRING
                    required: true
                variables:
                  model: gpt-4o-mini
                tasks:
                  - id: answer
                    type: openai.chat
                    model: "{{ vars.model }}"
                    prompt: "Hello"
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);

        assertEquals("release-notes-copilot", flow.getFlowId());
        assertEquals("examples.getting-started", flow.getNamespace());
        assertEquals(InputType.STRING, flow.getInputs().get(0).getType());
        assertEquals("answer", flow.getTasks().get(0).getId());
        assertEquals("{{ vars.model }}", flow.getTasks().get(0).getConfig().get("model"));
    }

    @Test
    void execution_roundTrip_json() throws Exception {
        Execution original = sampleExecution();
        String json = mapper.writeValueAsString(original);
        Execution restored = mapper.readValue(json, Execution.class);

        assertEquals(original.getId(), restored.getId());
        assertEquals(ExecutionState.RUNNING, restored.getState());
        assertEquals(TriggerType.MANUAL, restored.getTriggerType());
        assertEquals(2, restored.getTaskRuns().size());
        assertTrue(restored.hasTaskCompleted("answer"));
        assertFalse(restored.hasTaskCompleted("pending-step"));
    }

    @Test
    void execution_hasTaskCompleted_respectsTerminalStates() {
        Execution execution = Execution.builder()
                .taskRuns(List.of(
                        TaskRun.builder().taskId("a").state(TaskRunState.RUNNING).build(),
                        TaskRun.builder().taskId("b").state(TaskRunState.SUCCESS).build()))
                .build();

        assertFalse(execution.hasTaskCompleted("a"));
        assertTrue(execution.hasTaskCompleted("b"));
    }

    @Test
    void kafkaPayloadModels_roundTrip() throws Exception {
        UUID executionId = UUID.randomUUID();

        LogEntry log = LogEntry.builder()
                .executionId(executionId)
                .level(LogLevel.INFO)
                .message("Task started")
                .metadata(Map.of("model", "gpt-4o"))
                .createdAt(Instant.parse("2026-05-24T10:00:00Z"))
                .build();

        TaskResult result = TaskResult.builder()
                .taskRunId(UUID.randomUUID())
                .executionId(executionId)
                .taskId("answer")
                .state(TaskRunState.SUCCESS)
                .outputs(Map.of("response", "done"))
                .tokensUsed(120)
                .costUsd(new BigDecimal("0.0012"))
                .build();

        ExecutionEvent event = ExecutionEvent.builder()
                .executionId(executionId)
                .state(ExecutionState.SUCCESS)
                .timestamp(Instant.parse("2026-05-24T10:01:00Z"))
                .totalTokens(120L)
                .build();

        assertEquals(log.getMessage(), mapper.readValue(mapper.writeValueAsString(log), LogEntry.class).getMessage());
        assertEquals(TaskRunState.SUCCESS, mapper.readValue(mapper.writeValueAsString(result), TaskResult.class).getState());
        assertEquals(ExecutionState.SUCCESS, mapper.readValue(mapper.writeValueAsString(event), ExecutionEvent.class).getState());
    }

    @Test
    void input_deserialize_defaultsFromYaml() throws Exception {
        String yaml = """
                id: demo
                namespace: examples
                inputs:
                  - id: region
                    type: STRING
                    defaults: US
                tasks:
                  - id: t1
                    type: core.log
                    message: ok
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);

        assertEquals("US", flow.getInputs().get(0).getDefaultValue());
        assertTrue(mapper.writeValueAsString(flow.getInputs().get(0)).contains("\"defaults\":\"US\""));
    }

    @Test
    void task_coreIf_deserialize_thenAndElse_fromYaml() throws Exception {
        String yaml = """
                id: branch-demo
                namespace: examples
                tasks:
                  - id: gate
                    type: core.if
                    condition: "{{ outputs.check.ok }}"
                    then:
                      - id: ok-path
                        type: core.log
                        message: green
                    else:
                      - id: err-path
                        type: core.log
                        message: red
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);
        Task gate = flow.getTasks().get(0);

        assertEquals("core.if", gate.getType());
        assertEquals("{{ outputs.check.ok }}", gate.getCondition());
        assertEquals(1, gate.getThen().size());
        assertEquals("ok-path", gate.getThen().get(0).getId());
        assertEquals(1, gate.getElseTasks().size());
        assertEquals("err-path", gate.getElseTasks().get(0).getId());
    }

    @Test
    void task_coreIf_roundTrip_json_serializesElseKey() throws Exception {
        Task original = Task.builder()
                .id("gate")
                .type("core.if")
                .condition("true")
                .then(List.of(Task.builder().id("t").type("core.log").build()))
                .elseTasks(List.of(Task.builder().id("e").type("core.log").build()))
                .build();

        String json = mapper.writeValueAsString(original);
        assertTrue(json.contains("\"else\""));
        assertFalse(json.contains("elseTasks"));

        Task restored = mapper.readValue(json, Task.class);
        assertEquals(1, restored.getElseTasks().size());
        assertEquals("e", restored.getElseTasks().get(0).getId());
    }

    @Test
    void task_coreParallel_deserialize_nestedTasks_fromYaml() throws Exception {
        String yaml = """
                id: parallel-demo
                namespace: examples
                tasks:
                  - id: check-services
                    type: core.parallel
                    tasks:
                      - id: server-a
                        type: http.request
                        url: "https://a.example/health"
                      - id: server-b
                        type: http.request
                        url: "https://b.example/health"
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);
        Task parallel = flow.getTasks().get(0);

        assertEquals("core.parallel", parallel.getType());
        assertEquals(2, parallel.getTasks().size());
        assertEquals("server-a", parallel.getTasks().get(0).getId());
        assertEquals("https://a.example/health", parallel.getTasks().get(0).getConfig().get("url"));
    }

    @Test
    void task_retry_deserialize_backoffAndInitialDelay_fromYaml() throws Exception {
        String yaml = """
                id: retry-demo
                namespace: examples
                tasks:
                  - id: flaky
                    type: openai.chat
                    retry:
                      maxAttempts: 3
                      backoff: exponential
                      initialDelay: "1s"
                    prompt: hi
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);
        RetryPolicy retry = flow.getTasks().get(0).getRetry();

        assertNotNull(retry);
        assertEquals(3, retry.getMaxAttempts());
        assertEquals(BackoffType.EXPONENTIAL, retry.getBackoff());
        assertEquals(Duration.ofSeconds(1), retry.getInitialDelay());
        assertEquals("hi", flow.getTasks().get(0).getConfig().get("prompt"));
    }

    @Test
    void task_coreForeach_deserialize_itemsAndTasks_fromYaml() throws Exception {
        String yaml = """
                id: loop-demo
                namespace: examples
                tasks:
                  - id: outreach
                    type: core.foreach
                    items: "{{ outputs.accounts.body }}"
                    tasks:
                      - id: email
                        type: openai.chat
                        prompt: "Hi {{ taskrun.value.name }}"
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);
        Task loop = flow.getTasks().get(0);

        assertEquals("core.foreach", loop.getType());
        assertEquals("{{ outputs.accounts.body }}", loop.getItems());
        assertEquals(1, loop.getTasks().size());
        assertEquals("email", loop.getTasks().get(0).getId());
    }

    @Test
    void task_ifCondition_skipField_fromYaml() throws Exception {
        String yaml = """
                id: skip-demo
                namespace: examples
                tasks:
                  - id: optional-step
                    type: core.log
                    if: "{{ inputs.runOptional }}"
                    message: ran
                """;

        Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);

        assertEquals("{{ inputs.runOptional }}", flow.getTasks().get(0).getIfCondition());
        assertEquals("ran", flow.getTasks().get(0).getConfig().get("message"));
    }

    @Test
    void flow_roundTrip_json_preservesUuidAndFlowIdKeys() throws Exception {
        Flow original = sampleFlow();
        String json = mapper.writeValueAsString(original);

        assertTrue(json.contains("\"uuid\""));
        assertTrue(json.contains("\"id\":\"release-notes-copilot\""));

        Flow restored = mapper.readValue(json, Flow.class);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getFlowId(), restored.getFlowId());
    }

    @Test
    void enum_isTerminal() {
        assertFalse(ExecutionState.RUNNING.isTerminal());
        assertTrue(ExecutionState.SUCCESS.isTerminal());
        assertTrue(TaskRunState.FAILED.isTerminal());
        assertFalse(TaskRunState.CREATED.isTerminal());
    }

    private static Flow sampleFlow() {
        return Flow.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .flowId("release-notes-copilot")
                .namespace("examples.getting-started")
                .version(1)
                .description("Answer from release notes")
                .labels(Map.of("team", "examples"))
                .inputs(List.of(Input.builder()
                        .id("question")
                        .type(InputType.STRING)
                        .required(true)
                        .build()))
                .variables(Map.of("model", "gpt-4o-mini"))
                .triggers(List.of(Trigger.builder()
                        .id("nightly")
                        .type("schedule.cron")
                        .config(Map.of("cron", "0 0 * * *"))
                        .build()))
                .tasks(List.of(Task.builder()
                        .id("answer")
                        .type("openai.chat")
                        .config(Map.of("model", "gpt-4o-mini", "prompt", "Hello"))
                        .build()))
                .createdAt(Instant.parse("2026-05-24T09:00:00Z"))
                .build();
    }

    private static Execution sampleExecution() {
        UUID executionId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        return Execution.builder()
                .id(executionId)
                .flowId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .namespace("examples.getting-started")
                .state(ExecutionState.RUNNING)
                .triggerType(TriggerType.MANUAL)
                .inputs(Map.of("question", "What shipped?"))
                .taskRuns(List.of(
                        TaskRun.builder()
                                .id(UUID.randomUUID())
                                .executionId(executionId)
                                .taskId("answer")
                                .taskType("openai.chat")
                                .state(TaskRunState.SUCCESS)
                                .outputs(Map.of("response", "Feature X"))
                                .build(),
                        TaskRun.builder()
                                .id(UUID.randomUUID())
                                .executionId(executionId)
                                .taskId("pending-step")
                                .taskType("core.log")
                                .state(TaskRunState.RUNNING)
                                .build()))
                .startedAt(Instant.parse("2026-05-24T10:00:00Z"))
                .totalTokens(0L)
                .build();
    }
}
