package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ApplicationReplacementDto {

    private final String personGravatarURL;
    private final String personName;
    private final String note;
    private final boolean pending;
    private final Duration hours;
    private final String workDays;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Time startTime;
    private final Time endTime;
    private final ZonedDateTime startDateWithTime;
    private final ZonedDateTime endDateWithTime;
    private final DayLength dayLength;
    private final DayOfWeek weekDayOfStartDate;
    private final DayOfWeek weekDayOfEndDate;

    @SuppressWarnings("java:S107") // number of parameters is ok here for the DTO
    private ApplicationReplacementDto(String personGravatarURL, String personName, String note, boolean pending,
                                      Duration hours, String workDays, LocalDate startDate, LocalDate endDate,
                                      Time startTime, Time endTime, ZonedDateTime startDateWithTime,
                                      ZonedDateTime endDateWithTime, DayLength dayLength, DayOfWeek weekDayOfStartDate,
                                      DayOfWeek weekDayOfEndDate) {

        this.personGravatarURL = personGravatarURL;
        this.personName = personName;
        this.note = note;
        this.pending = pending;
        this.hours = hours;
        this.workDays = workDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDateWithTime = startDateWithTime;
        this.endDateWithTime = endDateWithTime;
        this.dayLength = dayLength;
        this.weekDayOfStartDate = weekDayOfStartDate;
        this.weekDayOfEndDate = weekDayOfEndDate;
    }

    public String getPersonGravatarURL() {
        return personGravatarURL;
    }

    public String getPersonName() {
        return personName;
    }

    public String getNote() {
        return note;
    }

    public boolean isPending() {
        return pending;
    }

    public Duration getHours() {
        return hours;
    }

    public String getWorkDays() {
        return workDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public ZonedDateTime getStartDateWithTime() {
        return startDateWithTime;
    }

    public ZonedDateTime getEndDateWithTime() {
        return endDateWithTime;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public DayOfWeek getWeekDayOfStartDate() {
        return weekDayOfStartDate;
    }

    public DayOfWeek getWeekDayOfEndDate() {
        return weekDayOfEndDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String personGravatarURL;
        private String personName;
        private String note;
        private boolean pending;
        private Duration hours;
        private String workDays;
        private LocalDate startDate;
        private LocalDate endDate;
        private Time startTime;
        private Time endTime;
        private ZonedDateTime startDateWithTime;
        private ZonedDateTime endDateWithTime;
        private DayLength dayLength;
        private DayOfWeek weekDayOfStartDate;
        private DayOfWeek weekDayOfEndDate;

        private Builder() {
            //
        }

        public Builder personGravatarURL(String personGravatarURL) {
            this.personGravatarURL = personGravatarURL;
            return this;
        }

        public Builder personName(String personName) {
            this.personName = personName;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder pending(boolean pending) {
            this.pending = pending;
            return this;
        }

        public Builder hours(Duration hours) {
            this.hours = hours;
            return this;
        }

        public Builder workDays(String workDays) {
            this.workDays = workDays;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder startTime(Time startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Time endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder startDateWithTime(ZonedDateTime startDateWithTime) {
            this.startDateWithTime = startDateWithTime;
            return this;
        }

        public Builder endDateWithTime(ZonedDateTime endDateWithTime) {
            this.endDateWithTime = endDateWithTime;
            return this;
        }

        public Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public Builder weekDayOfStartDate(DayOfWeek weekDayOfStartDate) {
            this.weekDayOfStartDate = weekDayOfStartDate;
            return this;
        }

        public Builder weekDayOfEndDate(DayOfWeek weekDayOfEndDate) {
            this.weekDayOfEndDate = weekDayOfEndDate;
            return this;
        }

        public ApplicationReplacementDto build() {
            return new ApplicationReplacementDto(personGravatarURL, personName, note, pending, hours, workDays,
                startDate, endDate, startTime, endTime, startDateWithTime, endDateWithTime, dayLength,
                weekDayOfStartDate, weekDayOfEndDate);
        }
    }
}
