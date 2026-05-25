package io.orchestrai.core.jackson;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Parses YAML durations such as {@code 1s}, {@code 30s}, {@code 10m}, {@code 2h}, {@code 1d}.
 * Also accepts ISO-8601 duration strings understood by {@link Duration#parse(CharSequence)}.
 */
public class HumanDurationDeserializer extends JsonDeserializer<Duration> {

    private static final Pattern HUMAN = Pattern.compile("^(\\d+)([smhd])$", Pattern.CASE_INSENSITIVE);

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = HUMAN.matcher(text.trim());
        if (matcher.matches()) {
            long amount = Long.parseLong(matcher.group(1));
            return switch (matcher.group(2).toLowerCase()) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                default -> throw ctxt.weirdStringException(text, Duration.class, "unsupported duration unit");
            };
        }
        return Duration.parse(text.trim());
    }
}
