package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Unit test for {@link Absence}.
 */
class AbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration timeConfiguration;
    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {

        person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(16);

        timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
    }


    @Test
    void ensureCanBeInstantiatedWithCorrectProperties() {

        LocalDate start = LocalDate.of(2015, 9, 21);
        LocalDate end = LocalDate.of(2015, 9, 23);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay(clock.getZone()));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay(clock.getZone()).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }


    @Test
    void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        LocalDate start = LocalDate.of(2015, 10, 23);
        LocalDate end = LocalDate.of(2015, 10, 25);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay(clock.getZone()));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay(clock.getZone()).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }


    @Test
    void ensureCorrectTimeForMorningAbsence() {

        LocalDateTime today = LocalDate.now(clock.getZone()).atStartOfDay();
        ZonedDateTime start = today.withHour(8).atZone(clock.getZone());
        ZonedDateTime end = today.withHour(12).atZone(clock.getZone());
        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration, clock);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }


    @Test
    void ensureCorrectTimeForNoonAbsence() {

        LocalDateTime today = LocalDate.now(clock.getZone()).atStartOfDay();
        ZonedDateTime start = today.withHour(12).atZone(clock.getZone());
        ZonedDateTime end = today.withHour(16).atZone(clock.getZone());
        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration, clock);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }


    @Test
    void ensureIsAllDayForFullDayPeriod() {

        LocalDate start = LocalDate.now(clock);
        LocalDate end = start.plusDays(2);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        assertThat(absence.isAllDay()).isTrue();
    }

    @Test
    void ensureIsNotAllDayForMorningPeriod() {
        LocalDate today = LocalDate.now(clock);

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        assertThat(absence.isAllDay()).isFalse();    }

    @Test
    void ensureIsNotAllDayForNoonPeriod() {

        LocalDate today = LocalDate.now(clock);
        Period period = new Period(today, today, DayLength.NOON);
        Absence absence = new Absence(person, period, timeConfiguration, clock);
        assertThat(absence.isAllDay()).isFalse();
    }

    @Test
    void ensureThrowsOnNullPeriod() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(person, null, timeConfiguration, clock));
    }

    @Test
    void ensureThrowsOnNullPerson() {
        Period period = new Period(LocalDate.now(clock), LocalDate.now(clock), DayLength.FULL);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(null, period, timeConfiguration, clock));
    }

    @Test
    void ensureThrowsOnNullConfiguration() {
        Period period = new Period(LocalDate.now(clock), LocalDate.now(clock), DayLength.FULL);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(person, period, null, clock));
    }

    @Test
    void ensureCorrectEventSubject() {

        LocalDate today = LocalDate.now(clock);
        Period period = new Period(today, today, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);
        assertThat(absence.getEventSubject()).isEqualTo("Marlene Muster abwesend");
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Absence absence = new Absence(person, new Period(LocalDate.MIN, LocalDate.MAX.withYear(10), DayLength.FULL), new AbsenceTimeConfiguration(new CalendarSettings()), clock);

        final String absenceToString = absence.toString();
        assertThat(absenceToString).isEqualTo("Absence{startDate=-999999999-01-01T00:00Z," +
            " endDate=0011-01-01T00:00Z, person=Person{id='10'}, isAllDay=true}");
    }
}
