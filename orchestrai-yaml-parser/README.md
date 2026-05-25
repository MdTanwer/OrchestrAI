# orchestrai-yaml-parser

Parse and validate OrchestrAI flow YAML into `orchestrai-core` domain models.

## Usage

```java
FlowParser parser = new FlowParser();
Flow flow = parser.parse(Path.of("examples/01-hello-agent.yaml"));

ValidationResult result = new FlowValidator().validate(flow);
if (!result.isValid()) {
    result.getErrors().forEach(System.err::println);
}
```

## Validation

- Required: `id`, `namespace`, at least one `tasks` entry
- Patterns: flow `id` `^[a-z0-9-]+$`, namespace `^[a-z0-9.-]+$`
- Unique task/input/trigger ids within each sibling block
- Known task/trigger types (see `KnownPluginTypes`)
- Control flow: `core.if` / `core.parallel` / `core.foreach` structure rules

## Tests

```bash
mvn test -pl orchestrai-yaml-parser -am
```

Uses YAML files from `../examples/` (01, 02, 03, 05, 15).
