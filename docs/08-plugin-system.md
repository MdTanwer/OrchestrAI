# 08 — Plugin System

OrchestrAI features an extensible, plugin-based architecture modeled after Kestra. Every task in a workflow is executed by a specific plugin. Plugins define their configuration schema, implementation execution logic, and outputs.

---

## Plugin Interface

All plugins must implement the lightweight `Plugin` interface:

```java
package io.orchestrai.plugin.sdk;

public interface Plugin<C extends PluginConfig, O extends PluginOutput> {

    String type();                    // e.g., "openai.chat"

    String version();                 // e.g., "1.0.0"

    Class<C> configClass();

    O execute(C config, ExecutionContext ctx) throws PluginException;
}
```

---

## Example Plugin: OpenAI Chat

Below is a complete implementation of the built-in `OpenAiChatPlugin`:

```java
package io.orchestrai.plugins.openai;

import io.orchestrai.plugin.sdk.*;
import java.math.BigDecimal;

@PluginType("openai.chat")
public class OpenAiChatPlugin implements Plugin<OpenAiChatConfig, OpenAiChatOutput> {

    @Override
    public String type() {
        return "openai.chat";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public Class<OpenAiChatConfig> configClass() {
        return OpenAiChatConfig.class;
    }

    @Override
    public OpenAiChatOutput execute(OpenAiChatConfig config, ExecutionContext ctx) throws PluginException {
        // Late-bound secret resolution inside the worker sandbox (highly secure!)
        String apiKey = ctx.getSecret(config.getSecretKeyRef() != null ? config.getSecretKeyRef() : "OPENAI_API_KEY");

        try {
            OpenAiClient client = new OpenAiClient(apiKey);
            ChatResponse response = client.chat(
                config.getModel(),
                config.getPrompt(),
                config.getTemperature()
            );

            // When config.isStream(), publish token deltas to task-logs/SSE bus per chunk;
            // still return full OpenAiChatOutput when the provider stream completes.
            return OpenAiChatOutput.builder()
                .response(response.text())
                .tokensUsed(response.tokens())
                .costUsd(calculateCost(response.tokens(), config.getModel()))
                .build();
        } catch (Exception e) {
            throw new PluginException("OpenAI call failed", e);
        }
    }
}
```

### Config Schema class
```java
public class OpenAiChatConfig implements PluginConfig {
    @NotNull private String model;
    @NotNull private String prompt;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Boolean stream = false;       // Emit token_delta SSE events when true
    private String secretKeyRef = "OPENAI_API_KEY"; // Overridable key reference
}
```

### Output Schema class
```java
public class OpenAiChatOutput implements PluginOutput {
    private String response;
    private Integer tokensUsed;
    private BigDecimal costUsd;
}
```

---

## Core & Built-in Plugins

### 1. Core Plugins
Core plugins manage dynamic execution logic and are processed directly by the stateless Executor.

| Type | Description |
|------|-------------|
| `core.if` | Dynamic conditional branching. |
| `core.parallel` | Asynchronous concurrent task execution. |
| `core.foreach` | Dynamic lists loops. |
| `core.delay` | Reactive pause for a specified duration. |
| `core.set` | Set workflow-wide variable values. |
| `core.log` | Output messages to the execution logs. |

### 2. AI Plugins
Each AI plugin is provider-agnostic, allowing teams to swap model backends in the flow YAML simply by changing the task `type`.

| Type | Description |
|------|-------------|
| `openai.chat` | GPT-4o, GPT-3.5-turbo chat models. |
| `openai.embedding` | Vector embedding generation. |
| `anthropic.chat` | Claude 3, Sonnet, Opus. |
| `google.gemini` | Gemini Pro / Flash integrations. |
| `ollama.chat` | Local open LLM execution (LLaMA 3, Mistral) via Ollama. |

### 3. Integration Plugins
Connect workflows to databases and external tools:

| Type | Description |
|------|-------------|
| `http.request` | Make arbitrary REST/GraphQL HTTP requests. |
| `shell.exec` | Execute secure shell scripts. |
| `kafka.produce` | Push messages onto Kafka streams. |
| `postgres.query` | Transact SQL queries on a database. |
| `s3.upload` | Pipe files to Amazon S3. |

### 4. Human Plugins
Pause state machine flow for manual decisions:

| Type | Description |
|------|-------------|
| `human.approval` | Pauses flow run until accepted by an authorized reviewer. |
| `human.input` | Pauses flow run to request form inputs. |

---

## Custom Plugin Distribution & JVM Classloading

Custom plugins are developed by implementing the `Plugin` SDK interface and compiled as external JARs. 

### Classloader Isolation & Dependency Conflicts
To prevent JAR dependency conflicts (e.g. Plugin A using Jackson 2.15 while Plugin B uses Jackson 2.12):
1.  **Quarkus Classloader Isolation:** In JVM mode, OrchestrAI loads plugins from the `/plugins` directory using separate, isolated child classloaders.
2.  **GraalVM Native Image Compilations:** Dynamic JAR loading is **not supported** in GraalVM Native Image. All custom plugins must be added as compile-time dependencies to the worker module and registered inside `PluginRegistry` before Native image compilation. See [14 — Native Image](./14-native-image.md#reflection-plugins-registration) for details.
