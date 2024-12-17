package org.synyx.urlaubsverwaltung.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;
import java.util.List;


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
}
