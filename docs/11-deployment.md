# Deployment

## Overview

This guide covers deploying OrchestrAI in various environments, from development to production.

## Deployment Options

### Docker Deployment

#### Docker Compose (Development)
```yaml
version: '3.8'
services:
  orchestrAI:
    image: orchestrai/orchestrai:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/orchestrai
      - REDIS_URL=redis://redis:6379
    depends_on:
      - db
      - redis
  
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=orchestrai
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

#### Docker Compose (Production)
```yaml
version: '3.8'
services:
  orchestrAI:
    image: orchestrai/orchestrai:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/orchestrai
      - REDIS_URL=redis://redis:6379
      - ENVIRONMENT=production
      - LOG_LEVEL=info
    depends_on:
      - db
      - redis
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Kubernetes Deployment

#### Deployment Manifest
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: orchestrai
spec:
  replicas: 3
  selector:
    matchLabels:
      app: orchestrai
  template:
    metadata:
      labels:
        app: orchestrai
    spec:
      containers:
      - name: orchestrai
        image: orchestrai/orchestrai:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: orchestrai-secrets
              key: database-url
        - name: REDIS_URL
          valueFrom:
            secretKeyRef:
              name: orchestrai-secrets
              key: redis-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Service Manifest
```yaml
apiVersion: v1
kind: Service
metadata:
  name: orchestrai
spec:
  selector:
    app: orchestrai
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

#### ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: orchestrai-config
data:
  ENVIRONMENT: "production"
  LOG_LEVEL: "info"
  WORKER_COUNT: "4"
```

#### Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: orchestrai-secrets
type: Opaque
stringData:
  database-url: "postgresql://user:pass@db:5432/orchestrai"
  redis-url: "redis://redis:6379"
  api-key: "your-api-key"
```

### Cloud Deployment

#### AWS
- **EKS**: Kubernetes deployment on AWS
- **ECS**: Fargate deployment
- **RDS**: PostgreSQL database
- **ElastiCache**: Redis cache
- **ALB**: Load balancer

#### GCP
- **GKE**: Kubernetes deployment on GCP
- **Cloud Run**: Serverless deployment
- **Cloud SQL**: PostgreSQL database
- **Memorystore**: Redis cache
- **Cloud Load Balancing**: Load balancer

#### Azure
- **AKS**: Kubernetes deployment on Azure
- **Container Instances**: Container deployment
- **Azure Database**: PostgreSQL database
- **Azure Cache**: Redis cache
- **Application Gateway**: Load balancer

## Configuration

### Environment Variables
```bash
# Database
DATABASE_URL=postgresql://user:pass@host:5432/db

# Cache
REDIS_URL=redis://host:6379

# Application
ENVIRONMENT=production
LOG_LEVEL=info
PORT=8080
WORKER_COUNT=4

# Security
API_KEY=your-api-key
SECRET_KEY=your-secret-key

# External Services
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-...
```

### Database Setup
```bash
# Run migrations
orchestrai migrate up

# Create admin user
orchestrai users create --email admin@example.com --password pass
```

## Monitoring

### Health Checks
```bash
# Health endpoint
curl http://localhost:8080/health

# Ready endpoint
curl http://localhost:8080/ready
```

### Metrics
- Prometheus metrics endpoint: `/metrics`
- Custom dashboards in Grafana
- Alert rules for critical metrics

### Logging
- Structured JSON logging
- Log aggregation (ELK, Cloud Logging)
- Log levels: debug, info, warn, error

## Scaling

### Horizontal Scaling
- Increase replica count in Kubernetes
- Use auto-scaling based on CPU/memory
- Scale based on queue depth

### Vertical Scaling
- Increase resource limits
- Optimize database connections
- Tune cache size

## Backup and Recovery

### Database Backup
```bash
# PostgreSQL backup
pg_dump dbname > backup.sql

# Restore
psql dbname < backup.sql
```

### Configuration Backup
- Version control workflow definitions
- Backup secrets securely
- Document configuration changes

## Troubleshooting

### Common Issues

#### Database Connection Failed
- Check DATABASE_URL
- Verify database is accessible
- Check network policies

#### High Memory Usage
- Reduce worker count
- Increase memory limits
- Check for memory leaks

#### Slow Execution
- Check database performance
- Verify cache is working
- Review workflow complexity
