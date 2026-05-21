# 12 — Security

Security is deeply integrated into the OrchestrAI architecture, ensuring robust authentication, compartmentalized namespaces (RBAC), and bulletproof secrets isolation modeled after Kestra's secure execution design.

---

## Authentication

All programmatic and user interactions with the API Server require verification.

### JWT Authentication
User logins receive a short-lived **JSON Web Token (JWT)**.
*   **API Base URL:** `https://api.orchestrai.io/v1`
*   **Login Endpoint:** `POST /v1/auth/login`
*   **Header Format:** `Authorization: Bearer <jwt-token>`

**JWT Claims Payload Schema:**
```json
{
  "sub": "user-uuid-12345",
  "email": "developer@orchestrai.io",
  "namespace": "production.marketing",
  "roles": ["DEVELOPER"],
  "iat": 1705315200,
  "exp": 1705401600
}
```

### API Key Authentication
For external systems, CI/CD, or automated cron runs, developers can generate long-lived API keys:
*   **Generate Key:** `POST /v1/auth/api-keys` (body: `{ "name": "github-actions" }`)
*   **Authorization Header:** `Authorization: ApiKey orch_live_abc123xyz`

---

## Role-Based Access Control (RBAC)

OrchestrAI isolates flows, executions, and secrets by **Namespace** (e.g. `production.engineering` cannot see or modify `production.finance`).

### Permissions Matrix

| Action | ADMIN | DEVELOPER | OPERATOR | VIEWER |
|--------|-------|-----------|----------|--------|
| Create flow | Yes | Yes | No | No |
| Edit flow | Yes | Yes | No | No |
| Delete flow | Yes | No | No | No |
| Execute flow | Yes | Yes | Yes | No |
| View executions | Yes | Yes | Yes | Yes |
| View logs | Yes | Yes | Yes | Yes |
| Manage secrets | Yes | Yes | No | No |
| Manage users | Yes | No | No | No |

---

## Secrets Management (No-Leak Pipeline)

> [!CAUTION]
> Plaintext credentials must **never** travel through message queues (Kafka) or be written to execution histories (PostgreSQL).

### Storing Secrets
Secrets are uploaded directly to the API Server:
*   `POST /v1/secrets` (body: `{ "namespace": "default", "key": "OPENAI_API_KEY", "value": "sk-proj-abc123xyz..." }`)
*   The API Server encrypts the value using **AES-256-GCM** immediately.
*   The encrypted payload is stored in the database `secrets` table.
*   Plaintext secrets are never returned by any GET endpoint or log output.

### Using Secrets in Flows (Late-Binding Worker Decryption)
To ensure absolute security, **secret expressions are deprecated** at the orchestrator engine level (e.g. `apiKey: "{{ secret('KEY') }}"` is forbidden).
*   If decrypted by the engine, the cleartext API key would be printed inside the task's resolved input schemas, traveling across the Kafka `task-runs` queue in plaintext and storing it in PostgreSQL `task_runs.inputs` logs.

#### Secure Context-Driven Resolution
Instead, plugins load secrets dynamically **inside the worker sandbox at execution time**:

```yaml
# SECURE: The YAML flow contains NO secrets or dynamic secret expressions.
tasks:
  - id: write-summary
    type: openai.chat
    prompt: "Summarize this data..."
```

*   **Under the hood:**
    1.  The Execution Engine evaluates the task, notices it is `openai.chat`, resolves the prompt, and dispatches the task over Kafka. The payload contains NO api keys.
    2.  The Worker Node consumes the task and initiates the `OpenAiChatPlugin`.
    3.  During execution, the plugin runs:
        `String apiKey = ctx.getSecret("OPENAI_API_KEY");`
    4.  The Worker's secure environment context decrypts the secret from the local storage context using the AES-256-GCM key and maps it to the OpenAI Client on-the-fly.
    5.  The key is GC'd immediately after the API call completes and never touches any log file or queue.

#### Custom Secrets Mapping
If a team has multiple keys for the same service (e.g., standard API key vs marketing API key), they pass the *reference name*, not the secret value:

```yaml
tasks:
  - id: write-summary
    type: openai.chat
    secretKeyRef: "OPENAI_API_KEY_MARKETING"  # Decrypted inside worker as ctx.getSecret("OPENAI_API_KEY_MARKETING")
    prompt: "Summarize this..."
```

---

## Cryptographic Implementation (AES-256-GCM)

Secrets are encrypted using Java's standard cryptographic architecture:

```java
package io.orchestrai.core.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class SecretEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12; // 12-byte IV for GCM

    public static String encrypt(String plaintext, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        
        // Concat IV + Ciphertext
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String ciphertext, byte[] key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] iv = Arrays.copyOf(decoded, IV_LENGTH);
        byte[] data = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);
        
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        
        return new String(cipher.doFinal(data));
    }
}
```

---

## OWASP Top 10 Coverage

| Risk | Mitigation |
|------|------------|
| Injection | Strict YAML structural schema validations, parameterized database queries via Hibernate. |
| Broken Authentication | Statistically rigid JWT verification with refresh scopes + secure API keys. |
| Sensitive Data Exposure | AES-256-GCM encryption on all secrets, late-bound worker injection. |
| XXE | Complete XML parsing disablement across Jackson structures. |
| Broken Access Control | Granular namespace-level RBAC validation interceptors on every REST endpoint. |
| Security Misconfiguration | Environment-driven variables, zero default secrets in production Compose files. |
| XSS | Sanitized dashboard inputs, high-grade Content Security Policies (CSP). |
| Insecure Deserialization | Jackson strict mode validation with disallowed dynamic polymorphic scopes. |
| Vulnerable Components | Build-time validation (Dependabot/Snyk), GraalVM closed-world dependency trees. |
| Logging | Full audit logs on API actions, zero plaintext logging for task inputs. |
