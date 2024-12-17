package org.synyx.urlaubsverwaltung.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

public class DatedDurationShareDto {

    @Schema(example = "2024-12-31",
        format = DATE_PATTERN)
    private final String date;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(
        example = "PT8H",
        type = "string",
        format = "duration")
    private final Duration duration;

    DatedDurationShareDto(LocalDate date, Duration duration) {
        this.date = date.format(ofPattern(DATE_PATTERN));
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public Duration getDuration() {
        return duration;
    }
}
