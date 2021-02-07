package org.synyx.urlaubsverwaltung.overtime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Converter
public class DurationConverter implements AttributeConverter<Duration, Double> {

    @Override
    public Double convertToDatabaseColumn(Duration attribute) {
        return (double) attribute.toMinutes() / 60 ;
    }

    @Override
    public Duration convertToEntityAttribute(Double duration) {
        return Duration.of((long) (duration * 60), ChronoUnit.MINUTES);
    }
}
