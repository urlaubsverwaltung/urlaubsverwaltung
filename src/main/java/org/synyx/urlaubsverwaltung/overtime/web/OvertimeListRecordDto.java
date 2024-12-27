package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public final class OvertimeListRecordDto {

    enum OvertimeListRecordType {
        OVERTIME,
        ABSENCE,
    }

    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration duration;
    private final Map<Integer, Duration> durationByYear;
    private final Duration sum;
    private final String status;
    private final String color;
    private final String type;
    private final boolean isAllowedToEdit;

    OvertimeListRecordDto(Long id, LocalDate startDate, LocalDate endDate, Duration duration, Map<Integer, Duration> durationByYear, Duration sum, String status, String color, String type, boolean isAllowedToEdit) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.durationByYear = durationByYear;
        this.sum = sum;
        this.status = status;
        this.color = color;
        this.type = type;
        this.isAllowedToEdit = isAllowedToEdit;
    }

    OvertimeListRecordDto(OvertimeListRecordDto overtimeListRecordDto, Duration sum, Map<Integer, Duration> durationByYear) {
        this(overtimeListRecordDto.id, overtimeListRecordDto.startDate, overtimeListRecordDto.endDate, overtimeListRecordDto.getDuration(),
            durationByYear, sum, overtimeListRecordDto.getStatus(), overtimeListRecordDto.getColor(), overtimeListRecordDto.getType(), overtimeListRecordDto.isAllowedToEdit);
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

    public Map<Integer, Duration> getDurationByYear() {
        return durationByYear;
    }

    public Map<Integer, Duration> getDurationByYear(int withoutYear) {
        return durationByYear.entrySet().stream()
            .filter(e -> e.getKey() != withoutYear)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Duration getSum() {
        return sum;
    }

    public String getStatus() {
        return status;
    }

    public String getColor() {
        return color;
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
            && Objects.equals(status, that.status) && Objects.equals(color, that.color) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, duration, sum, status, color, type);
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
            ", color='" + color + '\'' +
            ", type='" + type + '\'' +
            ", isAllowedToEdit=" + isAllowedToEdit +
            '}';
    }
}
