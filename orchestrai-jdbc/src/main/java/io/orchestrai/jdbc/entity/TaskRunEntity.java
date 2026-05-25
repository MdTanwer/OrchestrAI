package io.orchestrai.jdbc.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import io.orchestrai.core.enums.TaskRunState;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_runs")
public class TaskRunEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "execution_id", nullable = false)
    public UUID executionId;

    @Column(name = "task_id", nullable = false, length = 100)
    public String taskId;

    @Column(name = "task_type", nullable = false, length = 100)
    public String taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    public TaskRunState state;

    @Column(name = "attempt", nullable = false)
    public int attempt = 1;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "inputs", columnDefinition = "jsonb")
    public Map<String, Object> inputs;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "outputs", columnDefinition = "jsonb")
    public Map<String, Object> outputs;

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "ended_at")
    public Instant endedAt;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "tokens_used")
    public Integer tokensUsed;

    @Column(name = "cost_usd", precision = 10, scale = 6)
    public BigDecimal costUsd;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "worker_id", length = 100)
    public String workerId;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
