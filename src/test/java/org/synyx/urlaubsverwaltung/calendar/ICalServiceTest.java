package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.branding.BrandingProperties;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.Month.AUGUST;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.calendar.CalendarAbsenceType.DEFAULT;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.CANCELLED;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.PUBLISHED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class ICalServiceTest {

    private ICalService sut;

    @Mock
    private MessageSource messageSource;
    @Mock
    private UserSettingsService userSettingsService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-18T10:00:00Z"), UTC);

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }

    @BeforeEach
    void setUp() {
        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        sut = new ICalService(calendarProperties, new BrandingProperties("Urlaubsverwaltung"), messageSource, userSettingsService, clock);
    }

    @Test
    void ensureProdIdUsesConfiguredApplicationName() {

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sutWithBranding = new ICalService(calendarProperties, new BrandingProperties("Abwesenheiten"), messageSource, userSettingsService, clock);

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final ByteArrayResource calendar = sutWithBranding.getCalendar("Abwesenheitskalender", List.of(), recipient);
        assertThat(convertCalendar(calendar)).contains("PRODID:-//Abwesenheiten//iCal4j 1.0//DE");
    }

    @Test
    void ensureProdIdIsUtf8EncodedAndEscapesSpecialCharactersOfTheApplicationName() {

        final CalendarProperties calendarProperties = new CalendarProperties();
        calendarProperties.setOrganizer("no-reply@example.org");
        final ICalService sutWithBranding = new ICalService(calendarProperties, new BrandingProperties("Ürlaub; a,b\\c"), messageSource, userSettingsService, clock);

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final ByteArrayResource calendar = sutWithBranding.getCalendar("Abwesenheitskalender", List.of(), recipient);

        // ";", "," and "\" are delimiters in an iCalendar TEXT value and have to be escaped
        assertThat(new String(calendar.getByteArray(), UTF_8))
            .contains("PRODID:-//Ürlaub\\; a\\,b\\\\c//iCal4j 1.0//DE");
    }

    @Test
    void ensureToGetCalendarForPersonAndNoAbsenceFound() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(), recipient);
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

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence fullDayAbsence = absence(recipient, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);
        when(messageSource.getMessage(eq(fullDayAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(fullDayAbsence), recipient);
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
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence morningAbsence = absence(recipient, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);
        when(messageSource.getMessage(eq(morningAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(morningAbsence), recipient);
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
                DTSTART:20190426T080000Z
                DTEND:20190426T120000Z
                SUMMARY:Marlene Muster abwesend
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence manyFullDayAbsence = absence(recipient, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);
        when(messageSource.getMessage(eq(manyFullDayAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence), recipient);

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
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence noonAbsence = absence(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(messageSource.getMessage(eq(noonAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), recipient);
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
                DTSTART:20190526T120000Z
                DTEND:20190526T160000Z
                SUMMARY:Marlene Muster abwesend
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void getCalendarForPersonForHalfDayNoonWithEuropeBerlinTimezone() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence noonAbsence = absence(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON, DEFAULT, "Europe/Berlin");
        when(messageSource.getMessage(eq(noonAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCalendar("Abwesenheitskalender", List.of(noonAbsence), recipient);
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
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void cancelSingleAppointment() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence noonAbsence = absence(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(messageSource.getMessage(eq(noonAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, CANCELLED, recipient);
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
                DTSTART:20190526T120000Z
                DTEND:20190526T160000Z
                SUMMARY:Marlene Muster abwesend
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void ensureCancellationOfDateBasedInviteCarriesTheUidOfInvitesSentBeforeTheStableUid() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence noonAbsence = absence(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(messageSource.getMessage(eq(noonAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getCancellationOfDateBasedInvite(noonAbsence, recipient);
        // the uid the invite of this absence carried before the uid became the id of the absence
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
                DTSTART:20190526T120000Z
                DTEND:20190526T160000Z
                SUMMARY:Marlene Muster abwesend
                UID:497ED5D042F718878138A3E2F8C3C35C
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void singleAppointment() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence noonAbsence = absence(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(messageSource.getMessage(eq(noonAbsence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(recipient.getNiceName() + " abwesend");

        final ByteArrayResource calendar = sut.getSingleAppointment(noonAbsence, PUBLISHED, recipient);
        assertThat(convertCalendar(calendar))
            .isEqualToIgnoringNewLines("""
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE
                CALSCALE:GREGORIAN
                X-MICROSOFT-CALSCALE:GREGORIAN
                BEGIN:VEVENT
                DTSTAMP:<removedByConversionMethod>
                DTSTART:20190526T120000Z
                DTEND:20190526T160000Z
                SUMMARY:Marlene Muster abwesend
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void appointmentOfOtherPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person recipient = new Person();
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);
        when(messageSource.getMessage(eq(absence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(person.getNiceName() + " abwesend");

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
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                TRANSP:TRANSPARENT
                X-MICROSOFT-CDO-BUSYSTATUS:FREE
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void appointmentOfOtherPersonWithoutEMailAddress() {

        final Person recipient = new Person();
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final Person person = new Person("muster", "Muster", "Marlene", null);
        final CalendarAbsence absence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);
        when(messageSource.getMessage(eq(absence.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn(person.getNiceName() + " abwesend");

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
                UID:application-42
                TRANSP:TRANSPARENT
                X-MICROSOFT-CDO-BUSYSTATUS:FREE
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void holidayReplacement() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));

        final CalendarAbsence holidayReplacement = holidayReplacement(recipient, toDateTime("2019-05-26"), toDateTime("2019-05-26"), FULL);
        when(messageSource.getMessage(eq(holidayReplacement.getCalendarAbsenceTypeMessageKey()), any(), eq(GERMAN))).thenReturn("Vertretung für " + recipient.getNiceName());

        final ByteArrayResource calendar = sut.getSingleAppointment(holidayReplacement, PUBLISHED, recipient);
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
                UID:application-42
                ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Marlene Muster:mailto:muster@example.org
                TRANSP:TRANSPARENT
                X-MICROSOFT-CDO-BUSYSTATUS:FREE
                SEQUENCE:29916600
                ORGANIZER:mailto:no-reply@example.org
                END:VEVENT
                END:VCALENDAR
                """);
    }

    @Test
    void ensureUidIsTheIdOfTheAbsenceAndNotItsDates() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(userSettingsService.getEffectiveLocale(List.of(recipient))).thenReturn(Map.of(recipient, GERMAN));
        when(messageSource.getMessage(any(), any(), eq(GERMAN))).thenReturn("Marlene Muster abwesend");

        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Etc/UTC");
        final CalendarAbsenceConfiguration timeConfig = new CalendarAbsenceConfiguration(timeSettings);
        final Period period = new Period(LocalDate.of(2026, AUGUST, 3), LocalDate.of(2026, AUGUST, 7), FULL);
        final Period movedPeriod = new Period(LocalDate.of(2026, AUGUST, 10), LocalDate.of(2026, AUGUST, 14), FULL);

        final ByteArrayResource allowed = sut.getSingleAppointment(new CalendarAbsence("application-42", recipient, period, timeConfig), PUBLISHED, recipient);
        final ByteArrayResource cancelledAfterMove = sut.getSingleAppointment(new CalendarAbsence("application-42", recipient, movedPeriod, timeConfig), CANCELLED, recipient);
        final ByteArrayResource sickNoteOnSameDates = sut.getSingleAppointment(new CalendarAbsence("sicknote-42", recipient, period, timeConfig), PUBLISHED, recipient);

        assertThat(convertCalendar(allowed)).contains("UID:application-42");
        assertThat(convertCalendar(cancelledAfterMove)).contains("UID:application-42");
        assertThat(convertCalendar(sickNoteOnSameDates)).contains("UID:sicknote-42");
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, DEFAULT);
    }

    private CalendarAbsence holidayReplacement(Person person, LocalDate start, LocalDate end, DayLength length) {
        return absence(person, start, end, length, CalendarAbsenceType.HOLIDAY_REPLACEMENT);
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length, CalendarAbsenceType absenceType) {
        return absence(person, start, end, length, absenceType, "Etc/UTC");
    }

    private CalendarAbsence absence(Person person, LocalDate start, LocalDate end, DayLength length, CalendarAbsenceType absenceType, String timeZoneId) {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId(timeZoneId);
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayEndHour(16);
        final CalendarAbsenceConfiguration timeConfig = new CalendarAbsenceConfiguration(timeSettings);

        final Period period = new Period(start, end, length);
        return new CalendarAbsence("application-42", person, period, timeConfig, absenceType);
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
