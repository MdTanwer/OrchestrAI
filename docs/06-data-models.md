# Data Models

## Core Data Models

### Workflow
```yaml
Workflow:
  id: string (UUID)
  name: string
  version: string
  description: string
  definition: YAML
  created_at: timestamp
  updated_at: timestamp
  created_by: string (user_id)
  status: enum (active, inactive, archived)
```

### Execution
```yaml
Execution:
  id: string (UUID)
  workflow_id: string
  workflow_version: string
  status: enum (pending, running, completed, failed, cancelled)
  started_at: timestamp
  completed_at: timestamp
  context: object
  input: object
  output: object
  error: string
  trigger_type: enum (schedule, event, webhook, manual)
  trigger_data: object
```

### StepExecution
```yaml
StepExecution:
  id: string (UUID)
  execution_id: string
  step_id: string
  status: enum (pending, running, completed, failed, skipped)
  started_at: timestamp
  completed_at: timestamp
  input: object
  output: object
  error: string
  retry_count: integer
  logs: array
```

### Agent
```yaml
Agent:
  id: string (UUID)
  name: string
  type: enum (llm, custom, hybrid)
  model: string
  configuration: object
  capabilities: array
  status: enum (active, inactive)
  created_at: timestamp
  updated_at: timestamp
```

### Plugin
```yaml
Plugin:
  id: string (UUID)
  name: string
  version: string
  type: enum (integration, transformer, validator)
  configuration: object
  enabled: boolean
  installed_at: timestamp
```

### Trigger
```yaml
Trigger:
  id: string (UUID)
  workflow_id: string
  type: enum (schedule, event, webhook, manual)
  configuration: object
  enabled: boolean
  last_triggered: timestamp
  next_trigger: timestamp
```

## Supporting Models

### User
```yaml
User:
  id: string (UUID)
  username: string
  email: string
  role: enum (admin, user, viewer)
  created_at: timestamp
  last_login: timestamp
```

### Secret
```yaml
Secret:
  id: string (UUID)
  name: string
  value: encrypted_string
  created_by: string (user_id)
  created_at: timestamp
  updated_at: timestamp
```

### Environment
```yaml
Environment:
  id: string (UUID)
  name: string
  variables: object
  created_at: timestamp
  updated_at: timestamp
```

### AuditLog
```yaml
AuditLog:
  id: string (UUID)
  user_id: string
  action: string
  resource_type: string
  resource_id: string
  timestamp: timestamp
  details: object
```

## Relationships

- Workflow → Executions (one-to-many)
- Execution → StepExecutions (one-to-many)
- Workflow → Triggers (one-to-many)
- Agent → StepExecutions (one-to-many)
- User → Workflows (one-to-many)
- User → Secrets (one-to-many)
- Environment → Variables (one-to-many)
