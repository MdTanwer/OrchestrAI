package io.orchestrai.core.model;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.orchestrai.core.enums.BackoffType;
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
public class RetryPolicy {

    @Builder.Default
    private int maxAttempts = 1;

    /** YAML key {@code backoff} (e.g. exponential, fixed, linear). */
    private BackoffType backoff;

    /** YAML key {@code initialDelay} (e.g. {@code "1s"}). */
    private Duration initialDelay;

    /** Optional cap between retries (YAML {@code delay} if used). */
    private Duration delay;
}
