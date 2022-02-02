package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;

import java.time.Duration;
import java.time.LocalDate;

public class OvertimeListRecordDto {

    enum OvertimeListRecordType {
        OVERTIME,
        ABSENCE,
    }

    private final Integer id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final String status;
    private final String overtimeListRecordType;
    private final LocalDate lastModificationDate;

    OvertimeListRecordDto(Integer id, LocalDate startDate, LocalDate endDate, Duration duration, ApplicationStatus status, OvertimeListRecordType overtimeListRecordType, LocalDate lastModificationDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.status = status.name();
        this.overtimeListRecordType = overtimeListRecordType.name();
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

    public String getStatus() {
        return status;
    }

    public String getOvertimeListRecordType() {
        return overtimeListRecordType;
    }

    public LocalDate getLastModificationDate() {
        return lastModificationDate;
    }
}
