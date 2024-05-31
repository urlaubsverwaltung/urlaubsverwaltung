package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class AbsenceOverviewDayType {

    private final boolean waitingSickNoteMorning;
    private final boolean waitingSickNoteNoon;
    private final boolean waitingSickNoteFull;

    private final boolean activeSickNoteMorning;
    private final boolean activeSickNoteNoon;
    private final boolean activeSickNoteFull;

    private final boolean absenceMorning;
    private final boolean absenceNoon;
    private final boolean absenceFull;

    private final boolean waitingAbsenceMorning;
    private final boolean waitingAbsenceNoon;
    private final boolean waitingAbsenceFull;

    private final boolean temporaryAllowedAbsenceMorning;
    private final boolean temporaryAllowedAbsenceNoon;
    private final boolean temporaryAllowedAbsenceFull;

    private final boolean allowedCancellationRequestedAbsenceMorning;
    private final boolean allowedCancellationRequestedAbsenceNoon;
    private final boolean allowedCancellationRequestedAbsenceFull;

    private final boolean publicHolidayMorning;
    private final boolean publicHolidayNoon;
    private final boolean publicHolidayFull;

    private final AbsenceOverviewDayTypeColor color;

    @SuppressWarnings("java:S107")
    // Methods should not have too many parameters -> builder below must be used for construction
    private AbsenceOverviewDayType(boolean waitingSickNoteMorning, boolean waitingSickNoteNoon, boolean waitingSickNoteFull,
                                   boolean activeSickNoteMorning, boolean activeSickNoteNoon, boolean activeSickNoteFull, boolean absenceMorning,
                                   boolean absenceNoon, boolean absenceFull, boolean waitingAbsenceMorning, boolean waitingAbsenceNoon,
                                   boolean waitingAbsenceFull, boolean temporaryAllowedAbsenceMorning, boolean temporaryAllowedAbsenceNoon,
                                   boolean temporaryAllowedAbsenceFull, boolean allowedCancellationRequestedAbsenceMorning,
                                   boolean allowedCancellationRequestedAbsenceNoon, boolean allowedCancellationRequestedAbsenceFull,
                                   boolean publicHolidayMorning, boolean publicHolidayNoon,
                                   boolean publicHolidayFull, AbsenceOverviewDayTypeColor color) {
        this.waitingSickNoteMorning = waitingSickNoteMorning;
        this.waitingSickNoteNoon = waitingSickNoteNoon;
        this.waitingSickNoteFull = waitingSickNoteFull;
        this.activeSickNoteMorning = activeSickNoteMorning;
        this.activeSickNoteNoon = activeSickNoteNoon;
        this.activeSickNoteFull = activeSickNoteFull;
        this.absenceMorning = absenceMorning;
        this.absenceNoon = absenceNoon;
        this.absenceFull = absenceFull;
        this.waitingAbsenceMorning = waitingAbsenceMorning;
        this.waitingAbsenceNoon = waitingAbsenceNoon;
        this.waitingAbsenceFull = waitingAbsenceFull;
        this.temporaryAllowedAbsenceMorning = temporaryAllowedAbsenceMorning;
        this.temporaryAllowedAbsenceNoon = temporaryAllowedAbsenceNoon;
        this.temporaryAllowedAbsenceFull = temporaryAllowedAbsenceFull;
        this.allowedCancellationRequestedAbsenceMorning = allowedCancellationRequestedAbsenceMorning;
        this.allowedCancellationRequestedAbsenceNoon = allowedCancellationRequestedAbsenceNoon;
        this.allowedCancellationRequestedAbsenceFull = allowedCancellationRequestedAbsenceFull;
        this.publicHolidayMorning = publicHolidayMorning;
        this.publicHolidayNoon = publicHolidayNoon;
        this.publicHolidayFull = publicHolidayFull;
        this.color = color;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isWaitingSickNoteMorning() {
        return waitingSickNoteMorning;
    }

    public boolean isWaitingSickNoteNoon() {
        return waitingSickNoteNoon;
    }

    public boolean isWaitingSickNoteFull() {
        return waitingSickNoteFull;
    }

    public boolean isActiveSickNoteMorning() {
        return activeSickNoteMorning;
    }

    public boolean isActiveSickNoteNoon() {
        return activeSickNoteNoon;
    }

    public boolean isActiveSickNoteFull() {
        return activeSickNoteFull;
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

    public boolean isWaitingAbsenceMorning() {
        return waitingAbsenceMorning;
    }

    public boolean isWaitingAbsenceNoon() {
        return waitingAbsenceNoon;
    }

    public boolean isWaitingAbsenceFull() {
        return waitingAbsenceFull;
    }

    public boolean isTemporaryAllowedAbsenceMorning() {
        return temporaryAllowedAbsenceMorning;
    }

    public boolean isTemporaryAllowedAbsenceNoon() {
        return temporaryAllowedAbsenceNoon;
    }

    public boolean isTemporaryAllowedAbsenceFull() {
        return temporaryAllowedAbsenceFull;
    }

    public boolean isAllowedCancellationRequestedAbsenceMorning() {
        return allowedCancellationRequestedAbsenceMorning;
    }

    public boolean isAllowedCancellationRequestedAbsenceNoon() {
        return allowedCancellationRequestedAbsenceNoon;
    }

    public boolean isAllowedCancellationRequestedAbsenceFull() {
        return allowedCancellationRequestedAbsenceFull;
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

    public AbsenceOverviewDayTypeColor getColor() {
        return color;
    }

    public static class Builder {

        private boolean waitingSickNoteMorning = false;
        private boolean waitingSickNoteNoon = false;
        private boolean waitingSickNoteFull = false;

        private boolean activeSickNoteMorning = false;
        private boolean activeSickNoteNoon = false;
        private boolean activeSickNoteFull = false;

        private boolean absenceMorning = false;
        private boolean absenceNoon = false;
        private boolean absenceFull = false;

        private boolean waitingAbsenceMorning = false;
        private boolean waitingAbsenceNoon = false;
        private boolean waitingAbsenceFull = false;

        private boolean temporaryAllowedAbsenceMorning = false;
        private boolean temporaryAllowedAbsenceNoon = false;
        private boolean temporaryAllowedAbsenceFull = false;

        private boolean allowedCancellationRequestedAbsenceMorning = false;
        private boolean allowedCancellationRequestedAbsenceNoon = false;
        private boolean allowedCancellationRequestedAbsenceFull = false;

        private boolean publicHolidayMorning = false;
        private boolean publicHolidayNoon = false;
        private boolean publicHolidayFull = false;

        private VacationTypeColor colorMorning;
        private VacationTypeColor colorNoon;
        private VacationTypeColor colorFull;

        public Builder waitingSickNoteMorning() {
            this.waitingSickNoteMorning = true;
            return this;
        }

        public Builder waitingSickNoteNoon() {
            this.waitingSickNoteNoon = true;
            return this;
        }

        public Builder waitingSickNoteFull() {
            this.waitingSickNoteFull = true;
            return this;
        }

        public Builder activeSickNoteMorning() {
            this.activeSickNoteMorning = true;
            return this;
        }

        public Builder activeSickNoteNoon() {
            this.activeSickNoteNoon = true;
            return this;
        }

        public Builder activeSickNoteFull() {
            this.activeSickNoteFull = true;
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

        public Builder waitingAbsenceMorning() {
            this.waitingAbsenceMorning = true;
            return this;
        }

        public Builder waitingAbsenceNoon() {
            this.waitingAbsenceNoon = true;
            return this;
        }

        public Builder waitingAbsenceFull() {
            this.waitingAbsenceFull = true;
            return this;
        }

        public Builder temporaryAllowedAbsenceMorning() {
            this.temporaryAllowedAbsenceMorning = true;
            return this;
        }

        public Builder temporaryAllowedAbsenceNoon() {
            this.temporaryAllowedAbsenceNoon = true;
            return this;
        }

        public Builder temporaryAllowedAbsenceFull() {
            this.temporaryAllowedAbsenceFull = true;
            return this;
        }

        public Builder allowedCancellationRequestedAbsenceMorning() {
            this.allowedCancellationRequestedAbsenceMorning = true;
            return this;
        }

        public Builder allowedCancellationRequestedAbsenceNoon() {
            this.allowedCancellationRequestedAbsenceNoon = true;
            return this;
        }

        public Builder allowedCancellationRequestedAbsenceFull() {
            this.allowedCancellationRequestedAbsenceFull = true;
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

        public Builder colorMorning(VacationTypeColor colorMorning) {
            this.colorMorning = colorMorning;
            return this;
        }

        public Builder colorNoon(VacationTypeColor colorNoon) {
            this.colorNoon = colorNoon;
            return this;
        }

        public Builder colorFull(VacationTypeColor colorFull) {
            this.colorFull = colorFull;
            return this;
        }

        public AbsenceOverviewDayType build() {
            return new AbsenceOverviewDayType(
                waitingSickNoteMorning,
                waitingSickNoteNoon,
                waitingSickNoteFull,
                activeSickNoteMorning,
                activeSickNoteNoon,
                activeSickNoteFull,
                absenceMorning,
                absenceNoon,
                absenceFull,
                waitingAbsenceMorning,
                waitingAbsenceNoon,
                waitingAbsenceFull,
                temporaryAllowedAbsenceMorning,
                temporaryAllowedAbsenceNoon,
                temporaryAllowedAbsenceFull,
                allowedCancellationRequestedAbsenceMorning,
                allowedCancellationRequestedAbsenceNoon,
                allowedCancellationRequestedAbsenceFull,
                publicHolidayMorning,
                publicHolidayNoon,
                publicHolidayFull,
                new AbsenceOverviewDayTypeColor(colorMorning, colorNoon, colorFull));
        }
    }
}
