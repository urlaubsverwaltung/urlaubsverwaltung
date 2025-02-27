package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.DEFAULT;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.HOLIDAY_REPLACEMENT;

public class CalendarAbsence {

    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final Person person;
    private final boolean isAllDay;
    private final CalendarAbsenceType calendarAbsenceType;

    public CalendarAbsence(Person person, Period period, AbsenceTimeConfiguration absenceTimeConfiguration) {
        this(person, period, absenceTimeConfiguration, DEFAULT);
    }

    public CalendarAbsence(Person person, Period period, AbsenceTimeConfiguration absenceTimeConfiguration, CalendarAbsenceType calendarAbsenceType) {

        this.person = person;
        this.calendarAbsenceType = calendarAbsenceType;

        final ZonedDateTime periodStartDate = period.startDate().atStartOfDay(ZoneId.of(absenceTimeConfiguration.getTimeZoneId()));
        final ZonedDateTime periodEndDate = period.endDate().atStartOfDay(ZoneId.of(absenceTimeConfiguration.getTimeZoneId()));

        switch (period.dayLength()) {
            case FULL -> {
                this.startDate = periodStartDate;
                this.endDate = periodEndDate.plusDays(1);
                this.isAllDay = true;
            }
            case MORNING -> {
                this.startDate = periodStartDate.plusHours(absenceTimeConfiguration.getMorningStart());
                this.endDate = periodEndDate.plusHours(absenceTimeConfiguration.getMorningEnd());
                this.isAllDay = false;
            }
            case NOON -> {
                this.startDate = periodStartDate.plusHours(absenceTimeConfiguration.getNoonStart());
                this.endDate = periodEndDate.plusHours(absenceTimeConfiguration.getNoonEnd());
                this.isAllDay = false;
            }
            default -> throw new IllegalArgumentException("Invalid day length!");
        }
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
