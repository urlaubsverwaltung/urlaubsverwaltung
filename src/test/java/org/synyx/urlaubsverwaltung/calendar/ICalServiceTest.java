package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

class ICalServiceTest {

    private ICalService sut;

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }

    @BeforeEach
    void setUp() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        sut = new ICalService(calendarProperties);
    }

    @Test
    void getCalendarForPersonAndNoAbsenceFound() {
        final List<Absence> absences = List.of();

        assertThatThrownBy(() -> sut.getCalendar("Abwesenheitskalender", absences))
            .isInstanceOf(CalendarException.class);
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Absence fullDayAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final File calendar = sut.getCalendar("Abwesenheitskalender", List.of(fullDayAbsence));

        assertThat(fileToString(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190326")

            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")
            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Absence morningAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final File calendar = sut.getCalendar("Abwesenheitskalender", List.of(morningAbsence));

        assertThat(fileToString(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190426T080000Z")
            .contains("DTEND:20190426T120000Z")

            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")
            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Absence manyFullDayAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final File calendar = sut.getCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence));

        assertThat(fileToString(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190326")
            .contains("DTEND;VALUE=DATE:20190402")

            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")
            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Absence noonAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final File calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(fileToString(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")

            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")
            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarNoOrganizerIfNotProvided() {

        final Absence noonAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final ICalService sut = new ICalService(new CalendarProperties());
        final File calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(fileToString(calendar))
            .doesNotContain("ORGANIZER:mailto:");
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(timeSettings);

        final Period period = new Period(start, end, length);
        return new Absence(person, period, timeConfig);
    }

    private String fileToString(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "";
        }
    }
}
