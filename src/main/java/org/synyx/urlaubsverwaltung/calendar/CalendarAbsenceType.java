package org.synyx.urlaubsverwaltung.calendar;

public enum CalendarAbsenceType {

    DEFAULT("calendar.absence.type.default"),
    HOLIDAY_REPLACEMENT("calendar.absence.type.holidayReplacement");

    private final String messageKey;

    CalendarAbsenceType(final String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
