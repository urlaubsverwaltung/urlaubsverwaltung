package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

class ApplicationForLeaveDetailDto {

    private Long id;
    private Person person;
    private ApplicationStatus status;
    private ApplicationForLeaveDetailVacationTypeDto vacationType;
    private LocalDate applicationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    // startWithTime
    // endWithTime
    private DayOfWeek weekDayOfStartDate;
    private DayOfWeek weekDayOfEndDate;
    private DayLength dayLength;
    private BigDecimal workDays;
    private Duration hours;
    private LocalDate editedDate;
    private LocalDate cancelDate;
    private boolean twoStageApproval;
    private String reason;
    private List<HolidayReplacementEntity> holidayReplacements;
    private boolean teamInformed;
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationForLeaveDetailVacationTypeDto getVacationType() {
        return vacationType;
    }

    public void setVacationType(ApplicationForLeaveDetailVacationTypeDto vacationType) {
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

    public boolean isTwoStageApproval() {
        return twoStageApproval;
    }

    public void setTwoStageApproval(boolean twoStageApproval) {
        this.twoStageApproval = twoStageApproval;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<HolidayReplacementEntity> getHolidayReplacements() {
        return holidayReplacements;
    }

    public void setHolidayReplacements(List<HolidayReplacementEntity> holidayReplacements) {
        this.holidayReplacements = holidayReplacements;
    }

    public boolean isTeamInformed() {
        return teamInformed;
    }

    public void setTeamInformed(boolean teamInformed) {
        this.teamInformed = teamInformed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
