package io.orchestrai.core.jackson;

import java.time.Duration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Shared Jackson configuration for OrchestrAI domain models.
 * Register with {@code @RegisterForReflection} in Quarkus apps for native image.
 */
public final class ObjectMappers {

    private ObjectMappers() {
    }

    public static ObjectMapper json() {
        return configure(new ObjectMapper());
    }

    public static ObjectMapper yaml() {
        return configure(new ObjectMapper(new YAMLFactory()));
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        SimpleModule durations = new SimpleModule();
        durations.addDeserializer(Duration.class, new HumanDurationDeserializer());
        return mapper
                .registerModule(new JavaTimeModule())
                .registerModule(durations)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
