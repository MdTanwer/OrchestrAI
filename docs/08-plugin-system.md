# 08 — Plugin System

## What is a Plugin?

A plugin is a self-contained Java class that implements a Task type. It defines:

- Configuration schema
- Execution logic
- Output structure

---

## Plugin Interface

```java
public interface Plugin<C extends PluginConfig, O extends PluginOutput> {

    String type();                    // e.g., "openai.chat"

    String version();                 // e.g., "1.0.0"

    Class<C> configClass();

    O execute(C config, ExecutionContext ctx) throws PluginException;
}
```

---

## Plugin Lifecycle

1. Worker starts
2. ServiceLoader discovers plugins (`META-INF/services`)
3. Plugins register with PluginRegistry
4. Worker receives task from Kafka
5. Registry looks up plugin by type
6. Plugin config is deserialized from JSON
7. `Plugin.execute()` is called
8. Output is serialized and published

---

## Example Plugin: OpenAI Chat

```java
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
    public OpenAiChatOutput execute(OpenAiChatConfig config, ExecutionContext ctx) {
        String apiKey = ctx.getSecret("OPENAI_API_KEY");

        OpenAiClient client = new OpenAiClient(apiKey);
        ChatResponse response = client.chat(
            config.getModel(),
            config.getPrompt(),
            config.getTemperature()
        );

        return OpenAiChatOutput.builder()
            .response(response.text())
            .tokensUsed(response.tokens())
            .costUsd(calculateCost(response.tokens(), config.getModel()))
            .build();
    }
}
```

### Config Class

```java
public class OpenAiChatConfig implements PluginConfig {
    @NotNull private String model;
    @NotNull private String prompt;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
}
```

### Output Class

```java
public class OpenAiChatOutput implements PluginOutput {
    private String response;
    private Integer tokensUsed;
    private BigDecimal costUsd;
}
```

---

## Built-in Plugins

### Core Plugins

| Type | Description |
|------|-------------|
| core.if | Conditional branching |
| core.parallel | Parallel execution |
| core.foreach | Iteration |
| core.delay | Sleep |
| core.set | Set variable |

### AI Plugins

| Type | Description |
|------|-------------|
| openai.chat | OpenAI chat completion |
| openai.embedding | OpenAI embeddings |
| anthropic.chat | Claude chat |
| google.gemini | Gemini |
| ollama.chat | Local LLM |

### Integration Plugins

| Type | Description |
|------|-------------|
| http.request | HTTP call |
| shell.exec | Shell command |
| kafka.produce | Publish to Kafka |
| postgres.query | SQL query |
| s3.upload | Upload to S3 |

### Human Plugins

| Type | Description |
|------|-------------|
| human.approval | Pause for approval |
| human.input | Pause for input |

---

## Plugin Validation

Each plugin's config is validated using:

- Bean Validation (`@NotNull`, `@Min`, etc.)
- JSON Schema (auto-generated from class)
- Custom validators

---

## Distribution

Plugins are packaged as JAR files and:

- Bundled with worker (built-in)
- Loaded from `/plugins` directory (custom)
- Hot-reloadable (future)
