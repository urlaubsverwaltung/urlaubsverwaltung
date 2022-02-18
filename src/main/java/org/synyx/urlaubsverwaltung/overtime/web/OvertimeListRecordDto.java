package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public final class OvertimeListRecordDto {

    enum OvertimeListRecordType {
        OVERTIME,
        ABSENCE,
    }

    private final Integer id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final Duration sum;
    private final String status;
    private final String type;
    private final boolean isAllowedToEdit;

    OvertimeListRecordDto(Integer id, LocalDate startDate, LocalDate endDate, Duration duration, Duration sum, String status, String type, boolean isAllowedToEdit) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.sum = sum;
        this.status = status;
        this.type = type;
        this.isAllowedToEdit = isAllowedToEdit;
    }

    OvertimeListRecordDto(OvertimeListRecordDto overtimeListRecordDto, Duration sum) {
        this(overtimeListRecordDto.id, overtimeListRecordDto.startDate, overtimeListRecordDto.endDate, overtimeListRecordDto.getDuration(),
            sum, overtimeListRecordDto.getStatus(), overtimeListRecordDto.getType(), overtimeListRecordDto.isAllowedToEdit);
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

    public boolean isNegative() {
        return duration.isNegative();
    }

    public boolean isPositive() {
        return !duration.isNegative() && !duration.isZero();
    }

    public Duration getSum() {
        return sum;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public boolean isAllowedToEdit() {
        return isAllowedToEdit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertimeListRecordDto that = (OvertimeListRecordDto) o;
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate)
            && Objects.equals(duration, that.duration) && Objects.equals(sum, that.sum)
            && Objects.equals(status, that.status) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, duration, sum, status, type);
    }

    @Override
    public String toString() {
        return "OvertimeListRecordDto{" +
            "id=" + id +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", duration=" + duration +
            ", sum=" + sum +
            ", status='" + status + '\'' +
            ", type='" + type + '\'' +
            ", isAllowedToEdit=" + isAllowedToEdit +
            '}';
    }
}
