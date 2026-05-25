package io.orchestrai.yaml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.orchestrai.core.jackson.ObjectMappers;
import io.orchestrai.core.model.Flow;

/**
 * Parses OrchestrAI flow YAML into {@link Flow} domain models (Jackson YAML + core types).
 */
public final class FlowParser {

    public Flow parse(String yaml) {
        try {
            Flow flow = ObjectMappers.yaml().readValue(yaml, Flow.class);
            flow.setYamlSource(yaml);
            return flow;
        } catch (IOException e) {
            throw new FlowParseException("Failed to parse flow YAML", e);
        }
    }

    public Flow parse(Path path) {
        try {
            String yaml = Files.readString(path, StandardCharsets.UTF_8);
            Flow flow = parse(yaml);
            if (flow.getFlowId() == null || flow.getFlowId().isBlank()) {
                flow.setFlowId(path.getFileName().toString().replaceFirst("\\.ya?ml$", ""));
            }
            return flow;
        } catch (IOException e) {
            throw new FlowParseException("Failed to read flow file: " + path, e);
        }
    }

    public Flow parse(InputStream inputStream) {
        try {
            String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parse(yaml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
