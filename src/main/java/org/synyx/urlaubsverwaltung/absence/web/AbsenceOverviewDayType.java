package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewDayType {

    private final boolean waitingVacationMorning;
    private final boolean waitingVacationNoon;
    private final boolean waitingVacationFull;

    private final boolean allowedVacationMorning;
    private final boolean allowedVacationNoon;
    private final boolean allowedVacationFull;

    private final boolean sickNoteMorning;
    private final boolean sickNoteNoon;
    private final boolean sickNoteFull;

    private final boolean absenceMorning;
    private final boolean absenceNoon;
    private final boolean absenceFull;

    private final boolean publicHolidayMorning;
    private final boolean publicHolidayNoon;
    private final boolean publicHolidayFull;

    @SuppressWarnings("java:S107") // Methods should not have too many parameters -> builder below must be used for construction
    private AbsenceOverviewDayType(boolean waitingVacationMorning, boolean waitingVacationNoon, boolean waitingVacationFull,
                           boolean allowedVacationMorning, boolean allowedVacationNoon, boolean allowedVacationFull,
                           boolean sickNoteMorning, boolean sickNoteNoon, boolean sickNoteFull, boolean absenceMorning,
                           boolean absenceNoon, boolean absenceFull, boolean publicHolidayMorning,
                           boolean publicHolidayNoon, boolean publicHolidayFull) {
        this.waitingVacationMorning = waitingVacationMorning;
        this.waitingVacationNoon = waitingVacationNoon;
        this.waitingVacationFull = waitingVacationFull;
        this.allowedVacationMorning = allowedVacationMorning;
        this.allowedVacationNoon = allowedVacationNoon;
        this.allowedVacationFull = allowedVacationFull;
        this.sickNoteMorning = sickNoteMorning;
        this.sickNoteNoon = sickNoteNoon;
        this.sickNoteFull = sickNoteFull;
        this.absenceMorning = absenceMorning;
        this.absenceNoon = absenceNoon;
        this.absenceFull = absenceFull;
        this.publicHolidayMorning = publicHolidayMorning;
        this.publicHolidayNoon = publicHolidayNoon;
        this.publicHolidayFull = publicHolidayFull;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isWaitingVacationMorning() {
        return waitingVacationMorning;
    }

    public boolean isWaitingVacationNoon() {
        return waitingVacationNoon;
    }

    public boolean isWaitingVacationFull() {
        return waitingVacationFull;
    }

    public boolean isAllowedVacationMorning() {
        return allowedVacationMorning;
    }

    public boolean isAllowedVacationNoon() {
        return allowedVacationNoon;
    }

    public boolean isAllowedVacationFull() {
        return allowedVacationFull;
    }

    public boolean isSickNoteMorning() {
        return sickNoteMorning;
    }

    public boolean isSickNoteNoon() {
        return sickNoteNoon;
    }

    public boolean isSickNoteFull() {
        return sickNoteFull;
    }

    public boolean isAbsenceMorning() {
        return absenceMorning;
    }

    public boolean isAbsenceNoon() {
        return absenceNoon;
    }

    public boolean isAbsenceFull() {
        return absenceFull;
    }

    public boolean isPublicHolidayMorning() {
        return publicHolidayMorning;
    }

    public boolean isPublicHolidayNoon() {
        return publicHolidayNoon;
    }

    public boolean isPublicHolidayFull() {
        return publicHolidayFull;
    }

    public static class Builder {
        private boolean waitingVacationMorning = false;
        private boolean waitingVacationNoon = false;
        private boolean waitingVacationFull = false;

        private boolean allowedVacationMorning = false;
        private boolean allowedVacationNoon = false;
        private boolean allowedVacationFull = false;

        private boolean sickNoteMorning = false;
        private boolean sickNoteNoon = false;
        private boolean sickNoteFull = false;

        private boolean absenceMorning = false;
        private boolean absenceNoon = false;
        private boolean absenceFull = false;

        private boolean publicHolidayMorning = false;
        private boolean publicHolidayNoon = false;
        private boolean publicHolidayFull = false;

        public Builder waitingVacationMorning() {
            this.waitingVacationMorning = true;
            return this;
        }

        public Builder waitingVacationNoon() {
            this.waitingVacationNoon = true;
            return this;
        }

        public Builder waitingVacationFull() {
            this.waitingVacationFull = true;
            return this;
        }

        public Builder allowedVacationMorning() {
            this.allowedVacationMorning = true;
            return this;
        }

        public Builder allowedVacationNoon() {
            this.allowedVacationNoon = true;
            return this;
        }

        public Builder allowedVacationFull() {
            this.allowedVacationFull = true;
            return this;
        }

        public Builder sickNoteMorning() {
            this.sickNoteMorning = true;
            return this;
        }

        public Builder sickNoteNoon() {
            this.sickNoteNoon = true;
            return this;
        }

        public Builder sickNoteFull() {
            this.sickNoteFull = true;
            return this;
        }

        public Builder absenceMorning() {
            this.absenceMorning = true;
            return this;
        }

        public Builder absenceNoon() {
            this.absenceNoon = true;
            return this;
        }

        public Builder absenceFull() {
            this.absenceFull = true;
            return this;
        }

        public Builder publicHolidayMorning() {
            this.publicHolidayMorning = true;
            return this;
        }

        public Builder publicHolidayNoon() {
            this.publicHolidayNoon = true;
            return this;
        }

        public Builder publicHolidayFull() {
            this.publicHolidayFull = true;
            return this;
        }

        public AbsenceOverviewDayType build() {
            return new AbsenceOverviewDayType(
                waitingVacationMorning,
                waitingVacationNoon,
                waitingVacationFull,
                allowedVacationMorning,
                allowedVacationNoon,
                allowedVacationFull,
                sickNoteMorning,
                sickNoteNoon,
                sickNoteFull,
                absenceMorning,
                absenceNoon,
                absenceFull,
                publicHolidayMorning,
                publicHolidayNoon,
                publicHolidayFull
            );
        }
    }
}
