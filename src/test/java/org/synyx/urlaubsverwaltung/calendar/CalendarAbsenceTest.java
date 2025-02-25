package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.absence.AbsenceType.HOLIDAY_REPLACEMENT;

class CalendarAbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration absenceTimeConfiguration;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {

        person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(10L);

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayEndHour(16);
        absenceTimeConfiguration = new AbsenceTimeConfiguration(timeSettings);
    }

    @Test
    void ensureCanBeInstantiatedWithCorrectProperties() {

        final LocalDate start = LocalDate.of(2015, 9, 21);
        final LocalDate end = LocalDate.of(2015, 9, 23);
        final Period period = new Period(start, end, DayLength.FULL);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay().atZone(ZoneOffset.UTC));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay().atZone(ZoneOffset.UTC).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }

    @Test
    void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        final LocalDate start = LocalDate.of(2015, 10, 23);
        final LocalDate end = LocalDate.of(2015, 10, 25);
        final Period period = new Period(start, end, DayLength.FULL);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay().atZone(ZoneOffset.UTC));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay().atZone(ZoneOffset.UTC).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }

    @Test
    void ensureCorrectTimeForMorningAbsence() {

        final ZonedDateTime today = LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC);
        final ZonedDateTime start = today.withHour(8);
        final ZonedDateTime end = today.withHour(12);
        final Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.MORNING);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }

    @Test
    void ensureCorrectTimeForNoonAbsence() {

        final ZonedDateTime today = LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC);
        final ZonedDateTime start = today.withHour(12);
        final ZonedDateTime end = today.withHour(16);
        final Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.NOON);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }

    @Test
    void ensureIsAllDayForFullDayPeriod() {

        final LocalDate start = LocalDate.now(clock);
        final LocalDate end = start.plusDays(2);
        final Period period = new Period(start, end, DayLength.FULL);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.isAllDay()).isTrue();
    }

    @Test
    void ensureIsNotAllDayForMorningPeriod() {

        final LocalDate today = LocalDate.now(clock);
        final Period period = new Period(today, today, DayLength.MORNING);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.isAllDay()).isFalse();
    }

    @Test
    void ensureIsNotAllDayForNoonPeriod() {

        final LocalDate today = LocalDate.now(clock);
        final Period period = new Period(today, today, DayLength.NOON);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.isAllDay()).isFalse();
    }

    @Test
    void ensureCorrectEventSubject() {

        final LocalDate today = LocalDate.now(clock);
        final Period period = new Period(today, today, DayLength.FULL);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration);
        assertThat(absence.getEventSubject()).isEqualTo("Marlene Muster abwesend");
    }

    @Test
    void ensureCorrectEventSubjectForHolidayReplacement() {

        final LocalDate today = LocalDate.now(clock);
        final Period period = new Period(today, today, DayLength.FULL);

        final CalendarAbsence absence = new CalendarAbsence(person, period, absenceTimeConfiguration, HOLIDAY_REPLACEMENT);
        assertThat(absence.getEventSubject()).isEqualTo("Vertretung für Marlene Muster");
    }

    @Test
    void toStringTest() {
        // Date where daylight saving time is relevant
        final LocalDate start = LocalDate.of(2015, 10, 23);
        final LocalDate end = LocalDate.of(2015, 10, 25);
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final CalendarAbsence absence = new CalendarAbsence(person, new Period(start, end, DayLength.FULL), new AbsenceTimeConfiguration(timeSettings));

        final String absenceToString = absence.toString();
        assertThat(absenceToString)
            .isEqualTo("Absence{startDate=2015-10-23T00:00Z[Etc/UTC], endDate=2015-10-26T00:00Z[Etc/UTC], person=Person{id='10'}, isAllDay=true, absenceType=DEFAULT}");
    }
}
