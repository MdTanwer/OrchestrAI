package io.orchestrai.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.orchestrai.core.enums.InputType;
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
public class Input {

    private String id;
    private InputType type;
    @Builder.Default
    private boolean required = false;
    private String description;

    /** YAML key {@code defaults} (see docs/04-yaml-schema.md). */
    @JsonProperty("defaults")
    private Object defaultValue;
}
