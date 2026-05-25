package io.orchestrai.core.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Retry backoff strategy from YAML {@code retry.backoff}.
 */
public enum BackoffType {

    @JsonProperty("exponential")
    EXPONENTIAL,

    @JsonProperty("fixed")
    FIXED,

    @JsonProperty("linear")
    LINEAR
}
