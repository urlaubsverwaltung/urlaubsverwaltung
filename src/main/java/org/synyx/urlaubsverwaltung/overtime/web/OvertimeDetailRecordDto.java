package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertimeDetailRecordDto that = (OvertimeDetailRecordDto) o;
        return Objects.equals(person, that.person) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, startDate, endDate, duration);
    }

    @Override
    public String toString() {
        return "OvertimeDetailRecordDto{" +
            "id=" + id +
            ", person=" + person +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", duration=" + duration +
            ", lastModificationDate=" + lastModificationDate +
            '}';
    }
}
