package io.orchestrai.jdbc.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import io.orchestrai.core.enums.ExecutionState;
import io.orchestrai.core.enums.TriggerType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "executions")
public class ExecutionEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    /** FK to {@code flows.id} (database UUID), not the YAML flow identifier string. */
    @Column(name = "flow_id", nullable = false)
    public UUID flowRefId;

    @Column(name = "namespace", nullable = false, length = 100)
    public String namespace;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    public ExecutionState state;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "inputs", columnDefinition = "jsonb")
    public Map<String, Object> inputs;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "outputs", columnDefinition = "jsonb")
    public Map<String, Object> outputs;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", length = 20)
    public TriggerType triggerType;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "trigger_data", columnDefinition = "jsonb")
    public Map<String, Object> triggerData;

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "ended_at")
    public Instant endedAt;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "total_cost_usd", precision = 10, scale = 6)
    public BigDecimal totalCostUsd;

    @Column(name = "total_tokens")
    public Long totalTokens;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
