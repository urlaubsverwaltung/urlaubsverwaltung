package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.DEFAULT;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.HOLIDAY_REPLACEMENT;

public class CalendarAbsence {

    private final String uid;
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final Person person;
    private final boolean isAllDay;
    private final CalendarAbsenceType calendarAbsenceType;

    public CalendarAbsence(String uid, Person person, Period period, CalendarAbsenceConfiguration absenceTimeConfiguration) {
        this(uid, person, period, absenceTimeConfiguration, DEFAULT);
    }

    /**
     * @param uid identifies the absence across all of its changes, e.g. "application-42",
     *            calendars move or remove the appointment of an earlier invite by it
     */
    public CalendarAbsence(String uid, Person person, Period period, CalendarAbsenceConfiguration absenceTimeConfiguration, CalendarAbsenceType calendarAbsenceType) {

        this.uid = uid;
        this.person = person;
        this.calendarAbsenceType = calendarAbsenceType;

        // the time zone list of the settings (java.util.TimeZone) contains short ids like "IST", which ZoneId only knows by SHORT_IDS
        final ZoneId zoneId = ZoneId.of(absenceTimeConfiguration.timeZoneId(), ZoneId.SHORT_IDS);
        final ZonedDateTime periodStartDate = period.startDate().atStartOfDay(zoneId);
        final ZonedDateTime periodEndDate = period.endDate().atStartOfDay(zoneId);

        switch (period.dayLength()) {
            case FULL -> {
                this.startDate = periodStartDate;
                this.endDate = periodEndDate.plusDays(1);
                this.isAllDay = true;
            }
            case MORNING -> {
                this.startDate = periodStartDate.with(absenceTimeConfiguration.morningStartTime());
                this.endDate = periodEndDate.with(absenceTimeConfiguration.morningEndTime());
                this.isAllDay = false;
            }
            case NOON -> {
                this.startDate = periodStartDate.with(absenceTimeConfiguration.noonStartTime());
                this.endDate = periodEndDate.with(absenceTimeConfiguration.noonEndTime());
                this.isAllDay = false;
            }
            default -> throw new IllegalArgumentException("Invalid day length!");
        }
    }

    public String getUid() {
        return uid;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public Person getPerson() {
        return person;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public boolean isHolidayReplacement() {
        return calendarAbsenceType == HOLIDAY_REPLACEMENT;
    }

    public String getCalendarAbsenceTypeMessageKey() {
        return calendarAbsenceType.getMessageKey();
    }

    @Override
    public String toString() {
        return "Absence{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            ", person=" + person +
            ", isAllDay=" + isAllDay +
            ", absenceType=" + calendarAbsenceType +
            '}';
    }
}
