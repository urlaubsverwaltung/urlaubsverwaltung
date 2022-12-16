package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;

public class ApplicationReplacementDto {

    private final ApplicationPersonDto person;
    private final String note;
    private final boolean pending;
    private final String duration;
    private final BigDecimal workDays;
    private final String durationOfAbsenceDescription;
    private final DayLength dayLength;

    @SuppressWarnings("java:S107") // number of parameters is ok here for the DTO
    private ApplicationReplacementDto(ApplicationPersonDto person, String note, boolean pending,
                                      String duration, BigDecimal workDays, String durationOfAbsenceDescription,
                                      DayLength dayLength) {

        this.person = person;
        this.note = note;
        this.pending = pending;
        this.duration = duration;
        this.workDays = workDays;
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

    public BigDecimal getWorkDays() {
        return workDays;
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
        private BigDecimal workDays;
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

        public Builder workDays(BigDecimal workDays) {
            this.workDays = workDays;
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
                durationOfAbsenceDescription, dayLength);
        }
    }
}
