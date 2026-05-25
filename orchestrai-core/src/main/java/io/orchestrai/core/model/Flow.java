package io.orchestrai.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Flow {

    /**
     * Database primary key. JSON/API field {@code uuid}; YAML flow id uses {@link #flowId} ({@code id}).
     * {@code FlowEntity} must map: {@code uuid} → {@code flows.id}, {@code id} → {@code flows.flow_id}.
     */
    @JsonProperty("uuid")
    private UUID id;

    /** User-defined flow identifier from YAML ({@code id: my-flow}). Not the DB UUID. */
    @JsonProperty("id")
    private String flowId;

    private String namespace;

    @Builder.Default
    private int version = 1;

    private String description;

    @Builder.Default
    private Map<String, String> labels = new LinkedHashMap<>();

    @Builder.Default
    private List<Input> inputs = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> variables = new LinkedHashMap<>();

    @Builder.Default
    private List<Trigger> triggers = new ArrayList<>();

    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Builder.Default
    private List<Task> onFailure = new ArrayList<>();

    private String yamlSource;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
}
