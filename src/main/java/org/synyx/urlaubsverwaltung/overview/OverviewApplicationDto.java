package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

class OverviewApplicationDto {

    private Long id;
    private Long personId;
    private ApplicationStatus status;
    private OverviewVacationTypDto vacationType;
    private LocalDate applicationDate;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ZonedDateTime startDateWithTime;
    private ZonedDateTime endDateWithTime;
    private DayOfWeek weekDayOfStartDate;
    private DayOfWeek weekDayOfEndDate;
    private DayLength dayLength;
    private BigDecimal workDays;
    private Duration hours;
    private LocalDate editedDate;
    private LocalDate cancelDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public OverviewVacationTypDto getVacationType() {
        return vacationType;
    }

    public void setVacationType(OverviewVacationTypDto vacationType) {
        this.vacationType = vacationType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ZonedDateTime getStartDateWithTime() {
        return startDateWithTime;
    }

    public void setStartDateWithTime(ZonedDateTime startDateWithTime) {
        this.startDateWithTime = startDateWithTime;
    }

    public ZonedDateTime getEndDateWithTime() {
        return endDateWithTime;
    }

    public void setEndDateWithTime(ZonedDateTime endDateWithTime) {
        this.endDateWithTime = endDateWithTime;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return weekDayOfStartDate;
    }

    public void setWeekDayOfStartDate(DayOfWeek weekDayOfStartDate) {
        this.weekDayOfStartDate = weekDayOfStartDate;
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return weekDayOfEndDate;
    }

    public void setWeekDayOfEndDate(DayOfWeek weekDayOfEndDate) {
        this.weekDayOfEndDate = weekDayOfEndDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public void setDayLength(DayLength dayLength) {
        this.dayLength = dayLength;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public void setWorkDays(BigDecimal workDays) {
        this.workDays = workDays;
    }

    public Duration getHours() {
        return hours;
    }

    public void setHours(Duration hours) {
        this.hours = hours;
    }

    public LocalDate getEditedDate() {
        return editedDate;
    }

    public void setEditedDate(LocalDate editedDate) {
        this.editedDate = editedDate;
    }

    public LocalDate getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(LocalDate cancelDate) {
        this.cancelDate = cancelDate;
    }

    @Override
    public String toString() {
        return "OverviewApplicationDto{" +
            "id=" + id +
            ", personId=" + personId +
            ", status=" + status +
            ", vacationType=" + vacationType +
            ", applicationDate=" + applicationDate +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", startDateWithTime=" + startDateWithTime +
            ", endDateWithTime=" + endDateWithTime +
            ", weekDayOfStartDate=" + weekDayOfStartDate +
            ", weekDayOfEndDate=" + weekDayOfEndDate +
            ", dayLength=" + dayLength +
            ", workDays=" + workDays +
            ", hours=" + hours +
            ", editedDate=" + editedDate +
            ", cancelDate=" + cancelDate +
            '}';
    }
}
