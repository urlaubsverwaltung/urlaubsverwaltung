package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.time.LocalDate;

public class OvertimeListRecordDto {

    private final Integer id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final LocalDate lastModificationDate;

    public OvertimeListRecordDto(Integer id, LocalDate startDate, LocalDate endDate, Duration duration, LocalDate lastModificationDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.lastModificationDate = lastModificationDate;
    }

    public Integer getId() {
        return id;
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
