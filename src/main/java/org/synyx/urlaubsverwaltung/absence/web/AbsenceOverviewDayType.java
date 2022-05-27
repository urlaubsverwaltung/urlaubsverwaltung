package org.synyx.urlaubsverwaltung.absence.web;

import static org.synyx.urlaubsverwaltung.absence.web.AbsenceOverviewDayTypeColor.absenceOverviewDayTypeColorNone;

public class AbsenceOverviewDayType {

    private final boolean sickNoteMorning;
    private final boolean sickNoteNoon;
    private final boolean sickNoteFull;

    private final boolean absenceMorning;
    private final boolean absenceNoon;
    private final boolean absenceFull;

    private final boolean waitingAbsenceMorning;
    private final boolean waitingAbsenceNoon;
    private final boolean waitingAbsenceFull;

    private final boolean publicHolidayMorning;
    private final boolean publicHolidayNoon;
    private final boolean publicHolidayFull;

    private final AbsenceOverviewDayTypeColor color;

    @SuppressWarnings("java:S107") // Methods should not have too many parameters -> builder below must be used for construction
    private AbsenceOverviewDayType(boolean sickNoteMorning, boolean sickNoteNoon, boolean sickNoteFull, boolean absenceMorning,
                                   boolean absenceNoon, boolean absenceFull, boolean waitingAbsenceMorning, boolean waitingAbsenceNoon,
                                   boolean waitingAbsenceFull, boolean publicHolidayMorning, boolean publicHolidayNoon,
                                   boolean publicHolidayFull, AbsenceOverviewDayTypeColor color) {
        this.sickNoteMorning = sickNoteMorning;
        this.sickNoteNoon = sickNoteNoon;
        this.sickNoteFull = sickNoteFull;
        this.absenceMorning = absenceMorning;
        this.absenceNoon = absenceNoon;
        this.absenceFull = absenceFull;
        this.waitingAbsenceMorning = waitingAbsenceMorning;
        this.waitingAbsenceNoon = waitingAbsenceNoon;
        this.waitingAbsenceFull = waitingAbsenceFull;
        this.publicHolidayMorning = publicHolidayMorning;
        this.publicHolidayNoon = publicHolidayNoon;
        this.publicHolidayFull = publicHolidayFull;
        this.color = color;
    }

    public static Builder builder() {
        return new Builder();
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

    public boolean isWaitingAbsenceMorning() {
        return waitingAbsenceMorning;
    }

    public boolean isWaitingAbsenceNoon() {
        return waitingAbsenceNoon;
    }

    public boolean isWaitingAbsenceFull() {
        return waitingAbsenceFull;
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

        private boolean sickNoteMorning = false;
        private boolean sickNoteNoon = false;
        private boolean sickNoteFull = false;

        private boolean absenceMorning = false;
        private boolean absenceNoon = false;
        private boolean absenceFull = false;

        private boolean waitingAbsenceMorning = false;
        private boolean waitingAbsenceNoon = false;
        private boolean waitingAbsenceFull = false;

        private boolean publicHolidayMorning = false;
        private boolean publicHolidayNoon = false;
        private boolean publicHolidayFull = false;

        private AbsenceOverviewDayTypeColor color = absenceOverviewDayTypeColorNone();

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

        public Builder color(AbsenceOverviewDayTypeColor color) {
            this.color = color;
            return this;
        }

        public AbsenceOverviewDayType build() {
            return new AbsenceOverviewDayType(
                sickNoteMorning,
                sickNoteNoon,
                sickNoteFull,
                absenceMorning,
                absenceNoon,
                absenceFull,
                waitingAbsenceMorning,
                waitingAbsenceNoon,
                waitingAbsenceFull,
                publicHolidayMorning,
                publicHolidayNoon,
                publicHolidayFull,
                color);
        }
    }
}
