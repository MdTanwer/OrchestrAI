# YAML Schema

## Workflow Schema

### Basic Structure

```yaml
name: workflow-name
version: "1.0"
description: Workflow description
triggers:
  - type: schedule
    cron: "0 * * * *"
steps:
  - id: step-1
    type: agent
    agent: agent-name
    input:
      data: value
```

### Schema Reference

#### Workflow Metadata
- `name` (string, required): Unique workflow identifier
- `version` (string, required): Semantic version
- `description` (string, optional): Human-readable description
- `tags` (array, optional): Classification tags

#### Triggers
- `type` (string, required): Trigger type (schedule, event, webhook, manual)
- `cron` (string, optional): Cron expression for schedule triggers
- `event` (string, optional): Event type for event triggers
- `webhook` (object, optional): Webhook configuration

#### Steps
- `id` (string, required): Unique step identifier
- `type` (string, required): Step type (agent, api, transform, condition)
- `name` (string, optional): Human-readable step name
- `depends_on` (array, optional): Step dependencies
- `input` (object, optional): Input data configuration
- `output` (object, optional): Output mapping
- `retry` (object, optional): Retry configuration
- `timeout` (integer, optional): Timeout in seconds

## Agent Schema

```yaml
name: agent-name
type: llm
model: gpt-4
system_prompt: You are a helpful assistant
temperature: 0.7
max_tokens: 2000
tools:
  - tool-name
```

## Plugin Schema

```yaml
name: plugin-name
version: "1.0"
type: integration
config:
  api_key: ${SECRET}
  endpoint: https://api.example.com
```

## Variable Reference

- `${variable}`: Reference a variable
- `${SECRET}`: Reference a secret
- `${step.step-id.output}`: Reference step output
- `${context.key}`: Reference context data
