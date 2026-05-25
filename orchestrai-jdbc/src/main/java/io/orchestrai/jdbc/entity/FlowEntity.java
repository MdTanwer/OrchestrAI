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

/**
 * Maps {@code flows} table.
 *
 * <p>Domain mapping ({@link io.orchestrai.core.model.Flow}):
 * <ul>
 *   <li>{@code id} ↔ JSON {@code uuid} ↔ DB {@code flows.id}</li>
 *   <li>{@code flowId} ↔ YAML/JSON {@code id} ↔ DB {@code flows.flow_id}</li>
 * </ul>
 */
@Entity
@Table(name = "flows")
public class FlowEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "flow_id", nullable = false, length = 100)
    public String flowId;

    @Column(name = "namespace", nullable = false, length = 100)
    public String namespace;

    @Column(name = "version", nullable = false)
    public int version = 1;

    @Column(name = "yaml_source", nullable = false, columnDefinition = "TEXT")
    public String yamlSource;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "parsed_json", nullable = false, columnDefinition = "jsonb")
    public String parsedJson;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @JdbcTypeCode(JsonColumnTypes.JSONB)
    @Column(name = "labels", columnDefinition = "jsonb")
    public Map<String, String> labels;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "created_by")
    public UUID createdBy;
}
