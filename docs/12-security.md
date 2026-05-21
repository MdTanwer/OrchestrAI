# 12 — Security

## Authentication

### JWT Authentication

OrchestrAI uses **JSON Web Tokens (JWT)** for authentication.

**Token structure:**

```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "namespace": "default",
  "roles": ["ADMIN", "DEVELOPER"],
  "iat": 1705315200,
  "exp": 1705401600
}
```

**How to get a token:**

```
POST /v1/auth/login
```

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "expiresIn": 86400
}
```

### API Key Authentication

For programmatic access (CI/CD, webhooks):

```bash
# Generate API key
POST /v1/auth/api-keys
{ "name": "my-ci-pipeline" }

# Use API key
Authorization: ApiKey orch_live_abc123xyz
```

---

## Role-Based Access Control (RBAC)

### Roles

| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| DEVELOPER | Create and edit flows, run executions |
| VIEWER | Read-only access |
| OPERATOR | Run flows, view executions |

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

## Secrets Management

### Storing Secrets

```
POST /v1/secrets
```

```json
{
  "namespace": "default",
  "key": "OPENAI_API_KEY",
  "value": "sk-abc123..."
}
```

Secrets are:

- Encrypted using AES-256-GCM before storage
- Never returned in plain text via API
- Injected into plugin context at runtime
- Never logged

### Using Secrets in Flows

```yaml
tasks:
  - id: chat
    type: openai.chat
    apiKey: "{{ secret('OPENAI_API_KEY') }}"
    prompt: "{{ inputs.query }}"
```

### Encryption Implementation

```java
public class SecretEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    public String encrypt(String plaintext, byte[] key) {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = generateIV();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.encode(concat(iv, encrypted));
    }

    public String decrypt(String ciphertext, byte[] key) {
        byte[] decoded = Base64.decode(ciphertext);
        byte[] iv = Arrays.copyOf(decoded, 12);
        byte[] data = Arrays.copyOfRange(decoded, 12, decoded.length);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(data));
    }
}
```

---

## Security Best Practices

### Input Validation

- All YAML inputs are sanitized
- Expression injection is prevented
- File paths are validated

### Network Security

- HTTPS only in production
- Kafka connections use TLS
- Database connections use SSL

### Rate Limiting

| Endpoint | Limit |
|----------|-------|
| POST /flows | 100 req/min per user |
| POST /execute | 50 req/min per user |
| GET endpoints | 1000 req/min per user |

### Audit Logging

Every action is logged:

```json
{
  "event": "EXECUTION_STARTED",
  "userId": "uuid",
  "namespace": "default",
  "flowId": "my-flow",
  "ip": "192.168.1.1",
  "timestamp": "2024-01-15T10:00:00Z"
}
```

---

## OWASP Top 10 Coverage

| Risk | Mitigation |
|------|------------|
| Injection | Input sanitization, parameterized queries |
| Broken Auth | JWT + refresh tokens + API keys |
| Sensitive Data | AES-256 encryption for secrets |
| XXE | Disable XML external entities |
| Broken Access | RBAC on every endpoint |
| Security Misconfiguration | Env-based config, no defaults |
| XSS | CSP headers, input sanitization |
| Insecure Deserialization | Jackson strict mode |
| Vulnerable Components | Dependabot, regular updates |
| Logging | Audit trail for all actions |
