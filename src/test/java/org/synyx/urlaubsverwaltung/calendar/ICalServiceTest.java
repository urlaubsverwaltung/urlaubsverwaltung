package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.CANCELLED;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.PUBLISHED;
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

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of());
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D");
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Absence fullDayAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(fullDayAbsence));
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190326")

            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Absence morningAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(morningAbsence));
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190426T080000Z")
            .contains("DTEND:20190426T120000Z")

            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Absence manyFullDayAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence));

        assertThat(convertCalendar(calendar))
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

            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Absence noonAbsence = absence(new Person("muster", "Muster", "Marlene", "muster@example.org"), toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")

            .contains("ORGANIZER:mailto:no-reply@example.org");
    }

    @Test
    void getCalendarPublishEvent() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);
        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("UID:497ED5D042F718878138A3E2F8C3C35C")
            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org");
    }

    @Test
    void cancelSingleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, CANCELLED);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("METHOD:CANCEL")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("UID:497ED5D042F718878138A3E2F8C3C35C")
            .contains("SEQUENCE:1")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org");
    }

    @Test
    void singleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, PUBLISHED);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("UID:497ED5D042F718878138A3E2F8C3C35C")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org");
    }

    @Test
    void holidayReplacement() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Absence holidayReplacement = holidayReplacement(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(holidayReplacement, PUBLISHED);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")

            .contains("SUMMARY:Vertretung für Marlene Muster")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190526")
            .contains("TRANSP:TRANSPARENT")
            .contains("UID:D2A4772AEB3FD20D5F6997FCD8F28719")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org");
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, AbsenceType.DEFAULT);
    }

    private Absence holidayReplacement(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, AbsenceType.HOLIDAY_REPLACEMENT);
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length, AbsenceType absenceType) {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(timeSettings);

        final Period period = new Period(start, end, length);
        return new Absence(person, period, timeConfig, absenceType);
    }

    private String convertCalendar(ByteArrayResource calendar) {
        return new String(calendar.getByteArray());
    }
}
