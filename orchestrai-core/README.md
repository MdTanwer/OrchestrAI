# orchestrai-core

Shared domain models (Flow, Execution, TaskRun, enums). **Framework-agnostic** — no Quarkus.

## Requirements

- **JDK 17** (matches root `pom.xml` `java.version`)
- **Lombok** (annotation processing at compile time)

Full commands (SDKMAN, `JAVA_HOME`, Maven, Quarkus dev): **[docs/16-developer-setup.md](../docs/16-developer-setup.md)**.

```bash
java -version   # must show 17.x
mvn test -pl orchestrai-core
```

## Lombok

Models use `@Data`, `@Builder`, `@Jacksonized` where Jackson round-trip matters.
`Task` and `Trigger` keep hand-written `@JsonAnySetter` for flat YAML plugin fields.

Register types with `@RegisterForReflection` in Quarkus deployable modules for native image.
