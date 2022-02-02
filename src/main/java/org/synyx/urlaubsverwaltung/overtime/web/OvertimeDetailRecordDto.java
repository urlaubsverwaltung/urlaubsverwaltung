package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.time.LocalDate;

public class OvertimeDetailRecordDto {

    private final Integer id;
    private final OvertimeDetailPersonDto person;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final LocalDate lastModificationDate;

    OvertimeDetailRecordDto(Integer id, OvertimeDetailPersonDto person, LocalDate startDate, LocalDate endDate, Duration duration, LocalDate lastModificationDate) {
        this.id = id;
        this.person = person;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.lastModificationDate = lastModificationDate;
    }

    public Integer getId() {
        return id;
    }

    public OvertimeDetailPersonDto getPerson() {
        return person;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDate getLastModificationDate() {
        return lastModificationDate;
    }
}
