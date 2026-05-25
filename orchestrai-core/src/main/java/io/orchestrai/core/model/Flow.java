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

    @JsonProperty("uuid")
    private UUID id;

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
