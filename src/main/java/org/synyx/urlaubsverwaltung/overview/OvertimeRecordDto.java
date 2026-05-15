package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

class OvertimeRecordDto {

    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final boolean isExternal;
    private final boolean isAllowedToEdit;

    OvertimeRecordDto(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        Duration duration,
        boolean isExternal,
        boolean isAllowedToEdit
    ) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.isExternal = isExternal;
        this.isAllowedToEdit = isAllowedToEdit;
    }

    public Long getId() {
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

    public boolean isNegative() {
        return duration.isNegative();
    }

    public boolean isPositive() {
        return !duration.isNegative() && !duration.isZero();
    }

    public boolean isExternal() {
        return isExternal;
    }

    public boolean isAllowedToEdit() {
        return isAllowedToEdit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OvertimeRecordDto that = (OvertimeRecordDto) o;
        return isExternal == that.isExternal && isAllowedToEdit == that.isAllowedToEdit
            && Objects.equals(id, that.id)
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startDate, endDate, duration, isExternal, isAllowedToEdit);
    }

    @Override
    public String toString() {
        return "OvertimeListRecordDto{" +
            "id=" + id +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", duration=" + duration +
            ", isExternal=" + isExternal +
            ", isAllowedToEdit=" + isAllowedToEdit +
            '}';
    }
}
