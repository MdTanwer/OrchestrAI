# 11 — Deployment

## Docker Compose (Local Dev)

```yaml
version: "3.8"

services:

  # PostgreSQL
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: orchestrai
      POSTGRES_USER: orchestrai
      POSTGRES_PASSWORD: orchestrai
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  # Kafka + Zookeeper
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    ports:
      - "9092:9092"

  # OrchestrAI API Server
  orchestrai-api:
    build: ./api-server
    depends_on:
      - postgres
      - kafka
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/orchestrai
      DB_USER: orchestrai
      DB_PASSWORD: orchestrai
      KAFKA_BOOTSTRAP: kafka:9092
      JWT_SECRET: change-me-in-production
    ports:
      - "8080:8080"

  # OrchestrAI Worker
  orchestrai-worker:
    build: ./worker
    depends_on:
      - kafka
      - postgres
    environment:
      KAFKA_BOOTSTRAP: kafka:9092
      DB_URL: jdbc:postgresql://postgres:5432/orchestrai
    deploy:
      replicas: 2

  # OrchestrAI UI
  orchestrai-ui:
    build: ./ui
    depends_on:
      - orchestrai-api
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
    ports:
      - "3000:3000"

  # Prometheus (Metrics)
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  # Grafana (Dashboard)
  grafana:
    image: grafana/grafana:latest
    depends_on:
      - prometheus
    ports:
      - "3001:3000"

volumes:
  postgres_data:
```

---

## Quick Start

```bash
# Clone repository
git clone https://github.com/yourname/orchestrai.git
cd orchestrai

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f orchestrai-api

# Open UI
open http://localhost:3000

# Open API
curl http://localhost:8080/v1/health

# Stop everything
docker-compose down
```

---

## Environment Variables

### API Server

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| DB_URL | Yes | — | PostgreSQL JDBC URL |
| DB_USER | Yes | — | DB username |
| DB_PASSWORD | Yes | — | DB password |
| KAFKA_BOOTSTRAP | Yes | — | Kafka bootstrap servers |
| JWT_SECRET | Yes | — | JWT signing secret |
| SERVER_PORT | No | 8080 | HTTP port |
| LOG_LEVEL | No | INFO | Log level |
| CORS_ORIGINS | No | * | Allowed CORS origins |

### Worker

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| KAFKA_BOOTSTRAP | Yes | — | Kafka bootstrap servers |
| DB_URL | Yes | — | PostgreSQL JDBC URL |
| WORKER_THREADS | No | 10 | Thread pool size |
| WORKER_ID | No | auto | Unique worker identifier |
| PLUGINS_DIR | No | /plugins | Custom plugins directory |

---

## Production Deployment (Kubernetes)

### Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: orchestrai
```

### API Server Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: orchestrai-api
  namespace: orchestrai
spec:
  replicas: 2
  selector:
    matchLabels:
      app: orchestrai-api
  template:
    metadata:
      labels:
        app: orchestrai-api
    spec:
      containers:
        - name: api
          image: orchestrai/api:latest
          ports:
            - containerPort: 8080
          env:
            - name: DB_URL
              valueFrom:
                secretKeyRef:
                  name: orchestrai-secrets
                  key: db-url
            - name: KAFKA_BOOTSTRAP
              value: "kafka:9092"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /v1/health
              port: 8080
            initialDelaySeconds: 30
          readinessProbe:
            httpGet:
              path: /v1/ready
              port: 8080
```

### Worker Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: orchestrai-worker
  namespace: orchestrai
spec:
  replicas: 3
  selector:
    matchLabels:
      app: orchestrai-worker
  template:
    metadata:
      labels:
        app: orchestrai-worker
    spec:
      containers:
        - name: worker
          image: orchestrai/worker:latest
          env:
            - name: KAFKA_BOOTSTRAP
              value: "kafka:9092"
            - name: WORKER_THREADS
              value: "20"
          resources:
            requests:
              memory: "1Gi"
              cpu: "500m"
            limits:
              memory: "2Gi"
              cpu: "1000m"
```

---

## Maven Build

```bash
# Build all modules
mvn clean package -DskipTests

# Build with tests
mvn clean verify

# Build Docker images
mvn clean package jib:build

# Run locally
mvn quarkus:dev -pl api-server
```

---

## Project Module Structure

```
orchestrai/
├── orchestrai-core/          # Shared domain models
├── orchestrai-yaml-parser/   # YAML → Domain model
├── orchestrai-engine/        # Execution logic
├── orchestrai-api-server/    # REST API (Quarkus)
├── orchestrai-worker/          # Task executor
├── orchestrai-scheduler/     # Trigger scheduler
├── orchestrai-plugin-sdk/    # Plugin interface
├── orchestrai-plugins/       # Built-in plugins
│   ├── plugin-openai/
│   ├── plugin-anthropic/
│   ├── plugin-http/
│   └── plugin-core/
├── orchestrai-ui/              # Next.js dashboard
├── orchestrai-cli/           # CLI tool
└── docker-compose.yml
```
