package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.sql.Time;
import java.time.Duration;

public class ApplicationReplacementDto {

    private final String personGravatarURL;
    private final String personName;
    private final String note;
    private final boolean pending;
    private final Duration hours;
    private final String workDays;
    private final Time startTime;
    private final Time endTime;
    private final String durationOfAbsenceDescription;
    private final DayLength dayLength;

    @SuppressWarnings("java:S107") // number of parameters is ok here for the DTO
    private ApplicationReplacementDto(String personGravatarURL, String personName, String note, boolean pending,
                                      Duration hours, String workDays, Time startTime, Time endTime,
                                      String durationOfAbsenceDescription, DayLength dayLength) {

        this.personGravatarURL = personGravatarURL;
        this.personName = personName;
        this.note = note;
        this.pending = pending;
        this.hours = hours;
        this.workDays = workDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
        this.dayLength = dayLength;
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

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public String getDurationOfAbsenceDescription() {
        return durationOfAbsenceDescription;
    }

    public DayLength getDayLength() {
        return dayLength;
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
        private Time startTime;
        private Time endTime;
        private String durationOfAbsenceDescription;
        private DayLength dayLength;

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

        public Builder startTime(Time startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Time endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder durationOfAbsenceDescription(String durationOfAbsenceDescription) {
            this.durationOfAbsenceDescription = durationOfAbsenceDescription;
            return this;
        }

        public Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        public ApplicationReplacementDto build() {
            return new ApplicationReplacementDto(personGravatarURL, personName, note, pending, hours, workDays,
                startTime, endTime, durationOfAbsenceDescription, dayLength);
        }
    }
}
