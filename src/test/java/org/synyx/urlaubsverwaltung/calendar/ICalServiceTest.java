package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
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
    void ensureToGetCalendarForPersonAndNoAbsenceFound() {

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(), null);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(fullDayAbsence), person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART;VALUE=DATE:20190326
                SUMMARY:Marlene Muster abwesend
                X-MICROSOFT-CDO-ALLDAYEVENT:TRUE
                UID:E9C70D57B7F38AE3FDBB87019A1857B5
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence morningAbsence = absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(morningAbsence), person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190426T060000Z
                DTEND:20190426T100000Z
                SUMMARY:Marlene Muster abwesend
                UID:A4CAE46FDBA672FE9DCE5086F311A91E
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence manyFullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence), person);

        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART;VALUE=DATE:20190326
                DTEND;VALUE=DATE:20190402
                SUMMARY:Marlene Muster abwesend
                X-MICROSOFT-CDO-ALLDAYEVENT:TRUE
                UID:62841D8AD53F00626AE52D7835FBEB8D
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190526T100000Z
                DTEND:20190526T140000Z
                SUMMARY:Marlene Muster abwesend
                UID:203BCCB25ADDA1D45DAD353089374E99
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarPublishEvent() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);
        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                X-WR-CALNAME:Abwesenheitskalender
                REFRESH-INTERVAL:P1D
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190526T100000Z
                DTEND:20190526T140000Z
                SUMMARY:Marlene Muster abwesend
                UID:203BCCB25ADDA1D45DAD353089374E99
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void cancelSingleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final CalendarAbsence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, CANCELLED, person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                METHOD:CANCEL
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190526T100000Z
                DTEND:20190526T140000Z
                SUMMARY:Marlene Muster abwesend
                UID:203BCCB25ADDA1D45DAD353089374E99
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                SEQUENCE:1
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void singleAppointment() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        final CalendarAbsence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, PUBLISHED, person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190526T100000Z
                DTEND:20190526T140000Z
                SUMMARY:Marlene Muster abwesend
                UID:203BCCB25ADDA1D45DAD353089374E99
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void appointmentOfOtherPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);

        final Person recipient = new Person();
        recipient.setId(2L);

        final CalendarAbsence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(absence, PUBLISHED, recipient);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART;VALUE=DATE:20190526
                SUMMARY:Marlene Muster abwesend
                X-MICROSOFT-CDO-ALLDAYEVENT:TRUE
                UID:A8295F3ACFE8F939F49882443ECBC9A3
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                TRANSP:TRANSPARENT
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void appointmentOfOtherPersonWithoutEMailAddress() {

        final Person person = new Person("muster", "Muster", "Marlene", null);
        person.setId(1L);

        final Person recipient = new Person();
        recipient.setId(2L);

        final CalendarAbsence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(absence, PUBLISHED, recipient);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART;VALUE=DATE:20190526
                SUMMARY:Marlene Muster abwesend
                X-MICROSOFT-CDO-ALLDAYEVENT:TRUE
                UID:A8295F3ACFE8F939F49882443ECBC9A3
                TRANSP:TRANSPARENT
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void holidayReplacement() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final CalendarAbsence holidayReplacement = holidayReplacement(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sut = new ICalService(calendarProperties);

        final ByteArrayResource calendar = sut.getSingleAppointment(holidayReplacement, PUBLISHED, person);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART;VALUE=DATE:20190526
                SUMMARY:Vertretung für Marlene Muster
                X-MICROSOFT-CDO-ALLDAYEVENT:TRUE
                UID:39450CE1C2D4AA939E8F5D486CE90FCD
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                \s
                TRANSP:TRANSPARENT
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, AbsenceType.DEFAULT);
    }

    private CalendarAbsence holidayReplacement(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, AbsenceType.HOLIDAY_REPLACEMENT);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length, AbsenceType absenceType) {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Europe/Berlin");
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayEndHour(16);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(timeSettings);

        final Period period = new Period(start, end, length);
        return new CalendarAbsence(person, period, timeConfig, absenceType);
    }

    /**
     * Converts the calendar byte array to a string and removes the "DTSTAMP" value,
     * because this is different for every creation of a calendar
     *
     * @param calendar as byte array
     * @return calendar as string
     */
    private String convertCalendar(ByteArrayResource calendar) {
        return new String(calendar.getByteArray(), UTF_8)
            .replaceAll("(?m)^DTSTAMP.*", "DTSTAMP:<removedByConversionMethod>");
    }
}
