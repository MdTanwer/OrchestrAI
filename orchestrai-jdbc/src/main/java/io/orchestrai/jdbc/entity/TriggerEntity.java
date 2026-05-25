package io.orchestrai.jdbc.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "triggers")
public class TriggerEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    /** FK to {@code flows.id}. */
    @Column(name = "flow_id", nullable = false)
    public UUID flowRefId;

    @Column(name = "trigger_id", nullable = false, length = 100)
    public String triggerId;

    @Column(name = "type", nullable = false, length = 50)
    public String type;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    public Map<String, Object> config;

    @Column(name = "enabled", nullable = false)
    public boolean enabled = true;

    @Column(name = "last_triggered")
    public Instant lastTriggered;

    @Column(name = "next_trigger")
    public Instant nextTrigger;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
