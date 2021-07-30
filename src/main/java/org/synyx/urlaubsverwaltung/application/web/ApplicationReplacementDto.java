package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.sql.Time;

public class ApplicationReplacementDto {

    private final ApplicationPersonDto person;
    private final String note;
    private final boolean pending;
    private final String duration;
    private final String workDays;
    private final Time startTime;
    private final Time endTime;
    private final String durationOfAbsenceDescription;
    private final DayLength dayLength;

    @SuppressWarnings("java:S107") // number of parameters is ok here for the DTO
    private ApplicationReplacementDto(ApplicationPersonDto person, String note, boolean pending,
                                      String duration, String workDays, Time startTime, Time endTime,
                                      String durationOfAbsenceDescription, DayLength dayLength) {

        this.person = person;
        this.note = note;
        this.pending = pending;
        this.duration = duration;
        this.workDays = workDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationOfAbsenceDescription = durationOfAbsenceDescription;
        this.dayLength = dayLength;
    }

    public ApplicationPersonDto getPerson() {
        return person;
    }

    public String getNote() {
        return note;
    }

    public boolean isPending() {
        return pending;
    }

    public String getDuration() {
        return duration;
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
        private ApplicationPersonDto person;
        private String note;
        private boolean pending;
        private String duration;
        private String workDays;
        private Time startTime;
        private Time endTime;
        private String durationOfAbsenceDescription;
        private DayLength dayLength;

        private Builder() {
            //
        }

        public Builder person(ApplicationPersonDto person) {
            this.person = person;
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

        public Builder duration(String duration) {
            this.duration = duration;
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
            return new ApplicationReplacementDto(person, note, pending, duration, workDays,
                startTime, endTime, durationOfAbsenceDescription, dayLength);
        }
    }
}
