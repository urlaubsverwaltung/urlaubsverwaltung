package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.TimeSettings;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final TimeSettings timeSettings = new TimeSettings();

    @BeforeEach
    void setUp() {

        person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        timeSettings.setTimeZoneId("Etc/UTC");
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayEndHour(16);

        timeConfiguration = new AbsenceTimeConfiguration(timeSettings);
    }


    @Test
    void ensureCanBeInstantiatedWithCorrectProperties() {

        LocalDate start = LocalDate.of(2015, 9, 21);
        LocalDate end = LocalDate.of(2015, 9, 23);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay(ZoneId.of(timeSettings.getTimeZoneId())));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay(ZoneId.of(timeSettings.getTimeZoneId())).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }


    @Test
    void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        LocalDate start = LocalDate.of(2015, 10, 23);
        LocalDate end = LocalDate.of(2015, 10, 25);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

        assertThat(absence.getStartDate()).isEqualTo(start.atStartOfDay(ZoneId.of(timeSettings.getTimeZoneId())));
        assertThat(absence.getEndDate()).isEqualTo(end.atStartOfDay(ZoneId.of(timeSettings.getTimeZoneId())).plusDays(1));
        assertThat(absence.getPerson()).isEqualTo(person);
    }


    @Test
    void ensureCorrectTimeForMorningAbsence() {

        LocalDateTime today = LocalDate.now(ZoneId.of(timeSettings.getTimeZoneId())).atStartOfDay();
        ZonedDateTime start = today.withHour(8).atZone(ZoneId.of(timeSettings.getTimeZoneId()));
        ZonedDateTime end = today.withHour(12).atZone(ZoneId.of(timeSettings.getTimeZoneId()));
        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }


    @Test
    void ensureCorrectTimeForNoonAbsence() {

        LocalDateTime today = LocalDate.now(ZoneId.of(timeSettings.getTimeZoneId())).atStartOfDay();
        ZonedDateTime start = today.withHour(12).atZone(ZoneId.of(timeSettings.getTimeZoneId()));
        ZonedDateTime end = today.withHour(16).atZone(ZoneId.of(timeSettings.getTimeZoneId()));
        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.getStartDate()).isEqualTo(start);
        assertThat(absence.getEndDate()).isEqualTo(end);
    }


    @Test
    void ensureIsAllDayForFullDayPeriod() {

        LocalDate start = LocalDate.now(clock);
        LocalDate end = start.plusDays(2);
        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.isAllDay()).isTrue();
    }

    @Test
    void ensureIsNotAllDayForMorningPeriod() {
        LocalDate today = LocalDate.now(clock);

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.isAllDay()).isFalse();
    }

    @Test
    void ensureIsNotAllDayForNoonPeriod() {

        LocalDate today = LocalDate.now(clock);
        Period period = new Period(today, today, DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.isAllDay()).isFalse();
    }

    @Test
    void ensureThrowsOnNullPeriod() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(person, null, timeConfiguration));
    }

    @Test
    void ensureThrowsOnNullPerson() {
        Period period = new Period(LocalDate.now(clock), LocalDate.now(clock), DayLength.FULL);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(null, period, timeConfiguration));
    }

    @Test
    void ensureThrowsOnNullConfiguration() {
        Period period = new Period(LocalDate.now(clock), LocalDate.now(clock), DayLength.FULL);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Absence(person, period, null));
    }

    @Test
    void ensureCorrectEventSubject() {

        LocalDate today = LocalDate.now(clock);
        Period period = new Period(today, today, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);
        assertThat(absence.getEventSubject()).isEqualTo("Marlene Muster abwesend");
    }

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        // Date where daylight saving time is relevant
        LocalDate start = LocalDate.of(2015, 10, 23);
        LocalDate end = LocalDate.of(2015, 10, 25);
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final Absence absence = new Absence(person, new Period(start, end, DayLength.FULL), new AbsenceTimeConfiguration(timeSettings));

        final String absenceToString = absence.toString();
        assertThat(absenceToString).isEqualTo("Absence{startDate=2015-10-23T00:00Z[Etc/UTC]," +
            " endDate=2015-10-26T00:00Z[Etc/UTC], person=Person{id='10'}, isAllDay=true}");
    }
}
