# orchestrai-core

Shared domain models (Flow, Execution, TaskRun, enums). **Framework-agnostic** — no Quarkus.

## Requirements

- **JDK 17** (matches root `pom.xml` `java.version`)
- **Lombok** (annotation processing at compile time)

```bash
# SDKMAN (repo includes .sdkmanrc)
sdk env install   # or: sdk use java 17.0.17-tem

# Or system OpenJDK 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

## Build & test

```bash
java -version   # must show 17.x
mvn test -pl orchestrai-core
```

The `maven-enforcer-plugin` fails the build on JDK 21/25 with a clear message.

## Lombok

Models use `@Data`, `@Builder`, `@Jacksonized` where Jackson round-trip matters.
`Task` and `Trigger` keep hand-written `@JsonAnySetter` for flat YAML plugin fields.

Register types with `@RegisterForReflection` in Quarkus deployable modules for native image.
