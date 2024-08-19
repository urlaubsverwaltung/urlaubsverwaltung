package org.synyx.urlaubsverwaltung.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;

public class OvertimeAbsenceDto extends RepresentationModel<OvertimeAbsenceDto> {

    private final Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Duration usedOvertimeDuration;

    public OvertimeAbsenceDto(Long id, Duration usedOvertimeDuration) {
        this.id = id;
        this.usedOvertimeDuration = usedOvertimeDuration;
    }

    public Long getId() {
        return id;
    }

    public Duration getUsedOvertimeDuration() {
        return usedOvertimeDuration;
    }
}
