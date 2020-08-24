package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.calendar.config.ICalProperties;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

class ICalServiceTest {

    public static final int DAYS_IN_PAST_CONFIGURATION = 10;
    private ICalService sut;

    @BeforeEach
    void setUp() {
        var properties = new ICalProperties();
        properties.setDaysInPast(DAYS_IN_PAST_CONFIGURATION);
        sut = new ICalService(properties);
    }

    @Test
    void getCalendarForPersonAndNoAbsenceFound() {
        final List<Absence> absences = List.of();

        assertThatThrownBy(() -> sut.generateCalendar("Abwesenheitskalender", absences))
            .isInstanceOf(CalendarException.class);
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        var now = now();
        final Absence fullDayAbsence = absence(createPerson(), now, now, FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(fullDayAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        var now = now();
        final Absence morningAbsence = absence(createPerson(), now, now, MORNING);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(morningAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T080000Z")
            .contains("DTEND:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T120000Z");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        var startDate = now();
        var endDate = startDate.plusDays(5);
        final Absence manyFullDayAbsence = absence(createPerson(), startDate, endDate, FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:" + startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .contains("DTEND;VALUE=DATE:" + endDate.plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        var now = now();
        final Absence noonAbsence = absence(createPerson(), now, now, NOON);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T120000Z")
            .contains("DTEND:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T160000Z");
    }

    @Test
    void ensureFilteringOldAbsences() {

        var now = now();
        final Absence fullDayAbsence = absence(createPerson(), now, now, FULL);
        // adding + 2 is needed because fullday events end on next day
        var dateInPast = now.minusDays(DAYS_IN_PAST_CONFIGURATION + 2);
        final Absence olderAbsence = absence(createPerson(), dateInPast, dateInPast, FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(fullDayAbsence, olderAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .doesNotContain("DTSTART;VALUE=DATE:" + dateInPast.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    @Test
    void ensureNoFilterForAbsenceEndDateInRange() {

        var now = now();
        // adding +2 is needed because fullday events end on next day
        var startDate = now.minusDays(DAYS_IN_PAST_CONFIGURATION + 2);
        // this date is included in the filter
        var endDate = now.minusDays(DAYS_IN_PAST_CONFIGURATION + 1);
        final Absence olderAbsenceWithEndInRange = absence(createPerson(), startDate, endDate, FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(olderAbsenceWithEndInRange));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:" + startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .doesNotContain("DTSTART;VALUE=DATE:" + endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig);
    }
}
