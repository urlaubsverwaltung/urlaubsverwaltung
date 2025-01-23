package org.synyx.urlaubsverwaltung.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;


public class OvertimeAbsenceDto extends RepresentationModel<OvertimeAbsenceDto> {

    private final Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(
        example = "PT8H",
        type = "string",
        format = "duration")
    private final Duration duration;

    private final List<DatedDurationShareDto> durationShares;

    OvertimeAbsenceDto(Long id, Duration duration, List<DatedDurationShareDto> durationShares) {
        this.id = id;
        this.duration = duration;
        this.durationShares = durationShares;
    }

    public Long getId() {
        return id;
    }

    public Duration getDuration() {
        return duration;
    }

    public List<DatedDurationShareDto> getDurationShares() {
        return durationShares;
    }

    public static class DatedDurationShareDto {

        @Schema(example = "2024-12-31", format = DATE_PATTERN)
        private final String date;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(example = "PT8H", type = "string", format = "duration")
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
}
