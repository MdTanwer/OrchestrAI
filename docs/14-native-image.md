# 14 — Native Image (GraalVM)

OrchestrAI fully supports **GraalVM Native Image** compilation, allowing the API Server and Worker modules to compile into single, standalone native executable binaries.

---

## Startup and Memory Metrics

| Compilation Mode | JVM Mode | GraalVM Native Image |
|------------------|----------|----------------------|
| **Startup Time** | ~3–5 seconds | ~45–50 milliseconds |
| **Memory Footprint** | ~300MB RAM | ~40–50MB RAM |
| **Artifact size** | Fat JAR (~120MB) | Standalone Binary (~28MB) |
| **Peak Throughput** | Highly optimized | High (slightly lower peak) |
| **Build Time** | ~30 seconds | ~4–5 minutes |

---

## Native Image Limitations (Plugins & Reflection)

GraalVM uses a **closed-world assumption**. All bytecode must be analyzed at compile-time. Any dynamic features (reflection, class loading, proxies) require strict build-time configuration files.

### 1. JSON and YAML Reflection Config
Jackson requires reflection permissions to deserialize Flow YAML and inputs into Java classes. Quarkus makes this simple via `@RegisterForReflection`:

```java
package io.orchestrai.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(fields = true, methods = true, constructors = true)
public class Flow {
    // Registered for reflection automatically at compile time!
}
```

Alternatively, you can manually list classes in `src/main/resources/reflection-config.json`:
```json
[
  {
    "name": "io.orchestrai.core.model.Flow",
    "allDeclaredFields": true,
    "allDeclaredMethods": true,
    "allDeclaredConstructors": true
  }
]
```

### 2. The Dynamic Class Loading Limitation (Plugins)
*   **The Problem:** GraalVM Native Image **does not support dynamic class loading** at runtime. Loading arbitrary compiled custom plugin `.jar` files from a `/plugins` directory using ServiceLoader will **not work** on workers compiled to native executables.
*   **Solution A (Native Build-Time Registry):**
    Built-in and standard compile-time plugins must be registered inside the static plugin map at compile time, allowing the GraalVM compiler to analyze and include them in the binary tree:
    ```java
    package io.orchestrai.core.registry;
    
    import io.orchestrai.plugins.openai.OpenAiChatPlugin;
    import io.orchestrai.plugins.anthropic.AnthropicChatPlugin;
    import io.orchestrai.plugins.http.HttpPlugin;
    import jakarta.enterprise.context.ApplicationScoped;
    import java.util.Map;
    
    @ApplicationScoped
    public class PluginRegistry {
        
        private final Map<String, Class<? extends Plugin>> plugins = Map.of(
            "openai.chat",     OpenAiChatPlugin.class,
            "anthropic.chat",  AnthropicChatPlugin.class,
            "http.request",    HttpPlugin.class
        );
    }
    ```
*   **Solution B (Hybrid Dynamic Deployment):**
    If your deployment requires developers to dynamically drag-and-drop custom JAR files into `/plugins` at runtime, **you must deploy JVM-mode workers** rather than Native workers. Tasks are routed automatically via separate Kafka topics:
    *   `task-runs-native` -> Handled by GraalVM Worker nodes.
    *   `task-runs-jvm` -> Handled by JVM Worker nodes (supporting dynamic child classloading).

---

## Compiling Quarkus Native Binaries

### API Server Compilation
The API server uses Quarkus's built-in GraalVM plugin:
```bash
cd orchestrai-api-server
mvn clean package -Pnative
```
*   **Output:** `target/orchestrai-api-server-1.0.0-runner` (Executable binary)
*   **Run command:** `./target/orchestrai-api-server-1.0.0-runner`

### CLI Tool Native Compilation
The command-line tool utilizes Picocli and is compiled directly into a native executable, allowing users to run the CLI instantly without requiring a Java runtime (JRE):
```bash
cd orchestrai-cli
mvn clean package -Pnative
```
*   **Output:** `target/orchestrai` (Single 28MB file, distributed via Brew/Scoop)

---

## Native Container Build (Multi-Stage)

To package native binaries into lightweight containers, use the following `Dockerfile.native`:

```dockerfile
# Multi-stage build
FROM quay.io/quarkus/quarkus-micro-image:2.0

WORKDIR /work/
COPY --chown=1001:root target/*-runner /work/application

RUN chmod 775 /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
```

Build and run:
```bash
docker build -f Dockerfile.native -t orchestrai/api-native:latest .
docker run -p 8080:8080 orchestrai/api-native:latest
```
