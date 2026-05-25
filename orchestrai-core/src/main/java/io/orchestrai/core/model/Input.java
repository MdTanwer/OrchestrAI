package io.orchestrai.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    private Object defaultValue;
}
