# Terminology

## Core Concepts

### Workflow
A defined sequence of operations that accomplish a specific task. Workflows are composed of steps, conditions, and data flows.

### Agent
An autonomous AI entity that can perform tasks, make decisions, and interact with other agents or systems.

### Plugin
An extension module that adds functionality to OrchestrAI, such as integrations with external services or custom processing logic.

### Step
A single unit of work within a workflow. Steps can be agent tasks, API calls, data transformations, or other operations.

### Trigger
An event or condition that initiates workflow execution. Triggers can be scheduled, event-based, or manual.

### Context
The data and state that flows through a workflow, accessible to all steps within that workflow.

### Schema
The YAML structure that defines workflows, agents, and other OrchestrAI resources.

## Execution Terms

### Execution Engine
The core component that schedules, runs, and monitors workflow executions.

### Job
A single instance of a workflow execution.

### Task
A unit of work assigned to an agent or executed by the system.

### Pipeline
A series of connected workflows that process data in stages.

## Configuration Terms

### Blueprint
A reusable workflow template that can be instantiated with different parameters.

### Variable
A named value that can be referenced throughout a workflow configuration.

### Secret
Encrypted sensitive data (API keys, passwords) that can be securely referenced in workflows.

### Environment
A named configuration set (development, staging, production) with specific settings and resources.
