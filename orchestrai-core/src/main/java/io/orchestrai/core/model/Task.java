package io.orchestrai.core.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class Task {

    private static final Set<String> RESERVED = Set.of(
            "id", "type", "description", "timeout", "retry", "if", "condition",
            "tasks", "then", "else", "items", "maxConcurrency", "maxToolRounds", "tools", "stream"
    );

    private String id;
    private String type;
    private String description;
    private Duration timeout;
    private RetryPolicy retry;

    @JsonProperty("if")
    private String ifCondition;

    private String condition;

    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Builder.Default
    private List<Task> then = new ArrayList<>();

    @JsonProperty("else")
    @Builder.Default
    private List<Task> elseTasks = new ArrayList<>();

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
