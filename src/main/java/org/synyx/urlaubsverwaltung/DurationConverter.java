package org.synyx.urlaubsverwaltung;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Converter
public class DurationConverter implements AttributeConverter<Duration, Double> {

    @Override
    public @Nullable Double convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) {
            return null;
        }
        return (double) attribute.toMinutes() / 60;
    }

    @Override
    public @Nullable Duration convertToEntityAttribute(Double duration) {
        if (duration == null) {
            return null;
        }
        return Duration.of(Math.round(duration * 60), ChronoUnit.MINUTES);
    }
}
