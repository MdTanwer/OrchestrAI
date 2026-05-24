#!/usr/bin/env python3
"""Generate TODO placeholder text for empty OrchestrAI scaffold files."""

from pathlib import Path

ROOT = Path("/home/ubuntu/OrchestrAI")
SKIP = {"kestra", "node_modules", ".git", ".next", "scripts"}

DESCRIPTIONS = {
    "Flow": "Immutable flow definition model (id, namespace, tasks, triggers). See docs/06-data-models.md",
    "Task": "Single task node in a flow YAML (type, config, retry, timeout).",
    "Execution": "Runtime execution state (inputs, outputs, taskRuns, cost).",
    "TaskRun": "Individual task instance dispatched to workers via Kafka.",
    "TaskResult": "Worker completion payload published to task-results topic.",
    "LogEntry": "Structured log event streamed over Kafka task-logs topic.",
    "ExecutionEvent": "Live execution state transition for dashboard SSE.",
    "Input": "Flow input parameter definition from YAML.",
    "Trigger": "Flow trigger definition (cron, webhook, kafka).",
    "Secret": "Namespace-scoped secret metadata (value stored encrypted).",
    "PluginDescriptor": "Plugin catalog entry exposed via GET /plugins.",
    "ExecutionState": "Enum: CREATED, RUNNING, SUCCESS, FAILED, CANCELLED, PAUSED.",
    "TaskRunState": "Enum: CREATED, RUNNING, SUCCESS, FAILED, CANCELLED.",
    "TriggerType": "Enum: MANUAL, CRON, WEBHOOK, EVENT.",
    "SecretEncryption": "AES-256-GCM encrypt/decrypt for secrets store. See docs/12-security.md",
    "FlowParser": "Parse YAML into Flow model using Jackson YAML.",
    "FlowValidator": "Validate flow schema, plugin types, and expression syntax.",
    "ValidationResult": "Validation errors/warnings returned from FlowValidator.",
    "Executor": "Core stateless execution orchestrator (library). See docs/07-execution-engine.md",
    "ExecutionTriggerHandler": "Handle new/resumed executions from Kafka executions topic.",
    "WorkerResultHandler": "Apply task-results, evaluate next ready tasks.",
    "CommandHandler": "Handle pause/cancel/resume admin commands.",
    "ExecutionContext": "Runtime context: inputs, outputs, variables for expression resolution.",
    "JexlExpressionResolver": "Resolve {{ outputs.x }} expressions via JEXL.",
    "RetryPolicy": "Per-task retry count, delay, and backoff configuration.",
    "BackoffStrategy": "Exponential/fixed backoff between retry attempts.",
    "TaskGraphResolver": "Build task dependency graph from flow YAML.",
    "ReadyTaskEvaluator": "Determine which tasks are ready after a TaskResult arrives.",
    "FlowResource": "REST: POST/GET/PUT/DELETE /flows. See docs/10-api-design.md",
    "ExecutionResource": "REST: trigger, list, get, cancel, resume executions.",
    "SecretResource": "REST: POST/DELETE /secrets with RBAC.",
    "PluginResource": "REST: GET /plugins catalog.",
    "MetricsResource": "REST: GET /metrics/executions and /metrics/costs.",
    "WebhookResource": "REST: POST /webhooks/{namespace}/{flowId}.",
    "HealthResource": "REST: GET /health liveness/readiness probes.",
    "ExecutionProducer": "Kafka producer: publish Execution to executions topic.",
    "LogConsumer": "Kafka consumer: task-logs → bulk insert PostgreSQL + SSE fan-out.",
    "ExecutionEventConsumer": "Kafka consumer: execution-events → SSE dashboard updates.",
    "LogStreamResource": "SSE: GET /executions/{id}/logs/stream",
    "TokenStreamResource": "SSE: GET /executions/{id}/stream (token_delta events).",
    "JwtFilter": "Validate Bearer JWT on all /v1/* requests.",
    "RbacInterceptor": "Enforce role-based access per namespace.",
    "ExecutionConsumer": "Kafka consumer: executions topic → ExecutionTriggerHandler.",
    "TaskResultConsumer": "Kafka consumer: task-results topic → WorkerResultHandler.",
    "CommandConsumer": "Kafka consumer: admin commands on executions topic.",
    "TaskRunProducer": "Kafka producer: dispatch TaskRun to task-runs topic.",
    "ExecutionEventProducer": "Kafka producer: publish ExecutionEvent to execution-events topic.",
    "WorkerConsumer": "Kafka consumer: task-runs → PluginRunner.execute().",
    "TaskResultProducer": "Kafka producer: publish TaskResult after task completion.",
    "LogEventProducer": "Kafka producer: publish structured logs to task-logs topic.",
    "DeadLetterProducer": "Kafka producer: failed tasks to dead-letter topic.",
    "PluginRegistry": "Map task type strings to Plugin implementations.",
    "PluginRunner": "Sandboxed plugin execution with secret injection.",
    "SandboxContext": "Worker-side ExecutionContext with getSecret() support.",
    "TokenStreamPublisher": "Publish token_delta chunks when stream=true.",
    "CronScheduler": "Quartz/cron scheduler for schedule.cron triggers.",
    "CronTriggerEvaluator": "Compute next fire time for cron triggers.",
    "KafkaTriggerConsumer": "Consume external Kafka topic → trigger flow. See examples/17-*",
    "ExecutionPublisher": "Publish triggered execution to Kafka executions topic.",
    "KafkaTopics": "Topic constants: executions, task-runs, task-results, task-logs, etc.",
    "MessageSerde": "Jackson JSON serializer/deserializer for Kafka messages.",
    "ExecutionMessage": "Kafka payload for executions topic.",
    "TaskRunMessage": "Kafka payload for task-runs topic.",
    "TaskResultMessage": "Kafka payload for task-results topic.",
    "LogEntryMessage": "Kafka payload for task-logs topic.",
    "ExecutionEventMessage": "Kafka payload for execution-events topic.",
    "FlowEntity": "JPA/Panache entity mapping flows table.",
    "ExecutionEntity": "JPA/Panache entity mapping executions table.",
    "TaskRunEntity": "JPA/Panache entity mapping task_runs table.",
    "LogEntity": "JPA/Panache entity mapping logs table.",
    "SecretEntity": "JPA/Panache entity mapping secrets table.",
    "TriggerEntity": "JPA/Panache entity mapping triggers table.",
    "FlowRepository": "CRUD + lookup by namespace/flowId.",
    "ExecutionRepository": "Lock execution row, update state transitions.",
    "TaskRunRepository": "Persist task run attempts and outputs.",
    "LogRepository": "Bulk insert logs from LogConsumer buffer.",
    "SecretRepository": "Encrypted secret storage per namespace.",
    "TriggerRepository": "Trigger registration and last-triggered tracking.",
    "Plugin": "Plugin interface: type(), execute(config, ctx). See docs/08-plugin-system.md",
    "PluginConfig": "Base marker interface for plugin configuration POJOs.",
    "PluginOutput": "Base marker interface for plugin output POJOs.",
    "PluginException": "Checked exception for plugin execution failures.",
    "OpenAiChatPlugin": "Plugin type openai.chat — GPT chat + streaming.",
    "OpenAiChatConfig": "Config POJO for OpenAiChatPlugin.",
    "OpenAiChatOutput": "Output POJO: response, tokensUsed, costUsd.",
    "OpenAiEmbeddingsPlugin": "Plugin type openai.embeddings.",
    "AnthropicChatPlugin": "Plugin type anthropic.chat — Claude API.",
    "AnthropicChatConfig": "Config POJO for AnthropicChatPlugin.",
    "GeminiPlugin": "Plugin type google.gemini.",
    "OllamaChatPlugin": "Plugin type ollama.chat — local LLM.",
    "HttpRequestPlugin": "Plugin type http.request.",
    "HttpRequestConfig": "Config: method, url, headers, body.",
    "HttpRequestOutput": "Output: statusCode, body, headers.",
    "HumanApprovalPlugin": "Plugin type human.approval — HITL pause.",
    "KafkaProducePlugin": "Plugin type kafka.produce.",
    "PostgresQueryPlugin": "Plugin type postgres.query.",
    "ShellExecPlugin": "Plugin type shell.exec.",
    "ParallelPlugin": "Plugin type core.parallel.",
    "IfPlugin": "Plugin type core.if.",
    "ForeachPlugin": "Plugin type core.foreach.",
    "DelayPlugin": "Plugin type core.delay.",
    "LogPlugin": "Plugin type core.log.",
    "SetPlugin": "Plugin type core.set.",
    "CliApplication": "CLI entry point.",
    "FlowCommand": "CLI: flow validate|list|run.",
    "ExecutionCommand": "CLI: execution status|logs|cancel.",
    "PluginCommand": "CLI: plugin list.",
}

ENUMS = {"ExecutionState", "TaskRunState", "TriggerType"}
INTERFACES = {"Plugin", "PluginConfig", "PluginOutput"}
EXCEPTIONS = {"PluginException"}

SQL_MIGRATIONS = {
    "V1__create_flows.sql": "flows table",
    "V2__create_executions.sql": "executions table",
    "V3__create_task_runs.sql": "task_runs table",
    "V4__create_logs.sql": "logs table",
    "V5__create_secrets.sql": "secrets table",
    "V6__create_triggers.sql": "triggers table",
}

POM_DESC = {
    "pom.xml": "Parent POM — modules, Quarkus BOM. See docs/13-roadmap.md Week 1",
    "orchestrai-core": "Core domain models.",
    "orchestrai-yaml-parser": "YAML parser. Depends on orchestrai-core.",
    "orchestrai-engine": "Execution engine library.",
    "orchestrai-api-server": "Quarkus REST API deployable.",
    "orchestrai-executor": "Quarkus executor deployable.",
    "orchestrai-worker": "Quarkus worker deployable.",
    "orchestrai-scheduler": "Quarkus scheduler deployable.",
    "orchestrai-jdbc": "PostgreSQL persistence (Panache/Flyway).",
    "orchestrai-messaging": "Shared Kafka topics and message DTOs.",
    "orchestrai-plugin-sdk": "Plugin SDK interface.",
    "orchestrai-cli": "CLI tool module.",
    "orchestrai-plugins": "Built-in plugins aggregator POM.",
}


def pkg(path: Path) -> str:
    parts = path.parts
    return ".".join(parts[parts.index("java") + 1 : -1])


def module_name(path: Path) -> str:
    for part in path.parts:
        if part.startswith("orchestrai-"):
            return part
    return "orchestrai"


def java_file(path: Path) -> str:
    name = path.stem
    desc = DESCRIPTIONS.get(name, f"Implement {name}.")
    package = pkg(path)
    mod = module_name(path)

    if name in ENUMS:
        return f"""/**
 * TODO: Define {name}
 * Module: {mod}
 * {desc}
 */
package {package};

public enum {name} {{
    // TODO: add enum constants
}}
"""

    if name in INTERFACES:
        return f"""/**
 * TODO: Define {name}
 * {desc}
 */
package {package};

public interface {name} {{
    // TODO: add interface methods
}}
"""

    if name in EXCEPTIONS:
        return f"""/**
 * TODO: Define {name}
 * {desc}
 */
package {package};

public class {name} extends Exception {{
    // TODO: add constructors
}}
"""

    return f"""/**
 * TODO: Implement {name}
 * Module: {mod}
 * {desc}
 * @see docs/05-architecture.md
 */
package {package};

public class {name} {{
    // TODO: add implementation
}}
"""


def write(path: Path, content: str) -> bool:
    if path.exists() and path.stat().st_size > 0:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    return True


def main():
    count = 0
    for path in ROOT.rglob("*"):
        if not path.is_file() or any(p in path.parts for p in SKIP):
            continue
        if path.stat().st_size > 0:
            continue

        rel = path.relative_to(ROOT)
        suffix = path.suffix.lower()

        if suffix == ".java":
            if write(path, java_file(path)):
                count += 1
        elif suffix == ".sql":
            desc = SQL_MIGRATIONS.get(path.name, "see docs/06-data-models.md")
            content = f"-- TODO: {path.name}\n-- {desc}\n-- Copy schema from docs/06-data-models.md\n"
            if write(path, content):
                count += 1
        elif path.name == "application.properties":
            mod = module_name(path)
            content = (
                f"# TODO: Quarkus config for {mod}\n"
                f"# See docs/11-deployment.md\n\n"
                f"# quarkus.datasource.jdbc.url=\n"
                f"# kafka.bootstrap.servers=\n"
            )
            if write(path, content):
                count += 1
        elif path.name == "pom.xml":
            mod = path.parent.name if path.parent != ROOT else "parent"
            desc = POM_DESC.get(mod, f"Maven POM for {mod}.")
            if mod == "parent":
                desc = POM_DESC["pom.xml"]
            content = f"""<?xml version="1.0" encoding="UTF-8"?>
<!-- TODO: {mod}/pom.xml — {desc} -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
</project>
"""
            if write(path, content):
                count += 1
        elif suffix in (".yml", ".yaml") and "k8s" in path.parts:
            content = f"# TODO: Kubernetes manifest — {path.stem}\n# See docs/11-deployment.md\n"
            if write(path, content):
                count += 1
        elif path.name == "docker-compose.yml":
            content = (
                "# TODO: docker-compose for local dev\n"
                "# See docs/11-deployment.md\n"
                "# Services: postgres, kafka, api, executor, worker, scheduler, ui\n"
            )
            if write(path, content):
                count += 1
        elif "docker" in path.parts and path.name.startswith("Dockerfile"):
            svc = path.name.replace("Dockerfile.", "")
            content = f"# TODO: Dockerfile for {svc}\n# See docs/11-deployment.md\n"
            if write(path, content):
                count += 1
        elif path.name == "prometheus.yml":
            content = "# TODO: Prometheus scrape config\n# See docs/11-deployment.md\n"
            if write(path, content):
                count += 1
        elif path.name == "ci.yml":
            content = "# TODO: GitHub Actions CI\n# Jobs: mvn verify, pnpm build\n"
            if write(path, content):
                count += 1

    print(f"Backend/infra placeholders written: {count}")


if __name__ == "__main__":
    main()
