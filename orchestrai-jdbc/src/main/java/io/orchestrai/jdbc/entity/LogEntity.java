package io.orchestrai.jdbc.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import io.orchestrai.core.enums.LogLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "logs")
public class LogEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    public Long id;

    @Column(name = "execution_id", nullable = false)
    public UUID executionId;

    @Column(name = "task_run_id")
    public UUID taskRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    public LogLevel level;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    public String message;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "metadata", columnDefinition = "jsonb")
    public Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
