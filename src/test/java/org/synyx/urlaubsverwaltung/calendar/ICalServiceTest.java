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

import static java.nio.charset.StandardCharsets.UTF_8;
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

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(), null);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(fullDayAbsence), person);
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

            .contains("ORGANIZER:mailto:no-reply@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence morningAbsence = absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(morningAbsence), person);
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

            .contains("ORGANIZER:mailto:no-reply@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence manyFullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence), person);

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

            .contains("ORGANIZER:mailto:no-reply@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), person);
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

            .contains("ORGANIZER:mailto:no-reply@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void getCalendarPublishEvent() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);
        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), person);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")
            .contains("REFRESH-INTERVAL:P1D")

            .contains("UID:BB2267885BD8DF263E88D3062853E8A7")
            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void cancelSingleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, CANCELLED, person);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("METHOD:CANCEL")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("UID:BB2267885BD8DF263E88D3062853E8A7")
            .contains("SEQUENCE:1")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void singleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, PUBLISHED, person);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z")
            .contains("UID:BB2267885BD8DF263E88D3062853E8A7")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org")

            .doesNotContain("TRANS:TRANSPARENT");
    }

    @Test
    void appointmentOfOtherPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Person recipient = new Person();
        recipient.setId(2L);

        final Absence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(absence, PUBLISHED, recipient);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190526")
            .contains("TRANSP:TRANSPARENT")
            .contains("UID:22D8DC26F4271C049ED5601345B58D9C")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org");
    }

    @Test
    void appointmentOfOtherPersonWoithoutEMailAddress() {

        final Person person = new Person("muster", "Muster", "Marlene", null);
        person.setId(1L);

        final Person recipient = new Person();
        recipient.setId(2L);

        final Absence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(absence, PUBLISHED, recipient);
        assertThat(convertCalendar(calendar))
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190526")
            .contains("TRANSP:TRANSPARENT")
            .contains("UID:22D8DC26F4271C049ED5601345B58D9C")

            .contains("ORGANIZER:mailto:no-reply@example.org")
            .doesNotContain("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:null");
    }

    @Test
    void holidayReplacement() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Absence holidayReplacement = holidayReplacement(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(holidayReplacement, PUBLISHED, person);
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
        return new String(calendar.getByteArray(), UTF_8);
    }
}
