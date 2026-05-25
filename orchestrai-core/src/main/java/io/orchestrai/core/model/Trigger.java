package io.orchestrai.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trigger {

    private static final Set<String> RESERVED = Set.of("id", "type");

    private String id;
    private String type;

    @JsonIgnore
    @Builder.Default
    private Map<String, Object> config = new LinkedHashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getConfig() {
        return config;
    }

    @JsonAnySetter
    public void putConfigProperty(String name, Object value) {
        if (!RESERVED.contains(name)) {
            config.put(name, value);
        }
    }
}
