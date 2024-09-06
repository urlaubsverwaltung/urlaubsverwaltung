package org.synyx.urlaubsverwaltung.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;

public class OvertimeAbsenceDto extends RepresentationModel<OvertimeAbsenceDto> {

    private final Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(
        example = "PT8H",
        type = "string",
        format = "duration")
    private final Duration duration;

    OvertimeAbsenceDto(Long id, Duration duration) {
        this.id = id;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public Duration getDuration() {
        return duration;
    }
}
