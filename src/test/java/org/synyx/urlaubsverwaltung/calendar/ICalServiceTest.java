package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
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
        sut = new ICalService();
    }

    @Test
    void getCalendarForPersonAndNoAbsenceFound() {
        final List<Absence> absences = List.of();

        assertThatThrownBy(() -> sut.generateCalendar("Abwesenheitskalender", absences))
            .isInstanceOf(CalendarException.class);
    }

    @Test
    void getCalendarForPersonForOneFullDay() {

        final Absence fullDayAbsence = absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(fullDayAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190326");
    }

    @Test
    void getCalendarForPersonForHalfDayMorning() {

        final Absence morningAbsence = absence(createPerson(), toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(morningAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190426T080000Z")
            .contains("DTEND:20190426T120000Z");
    }

    @Test
    void getCalendarForPersonForMultipleFullDays() {

        final Absence manyFullDayAbsence = absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence));

        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE")
            .contains("DTSTART;VALUE=DATE:20190326")
            .contains("DTEND;VALUE=DATE:20190402");
    }

    @Test
    void getCalendarForPersonForHalfDayNoon() {

        final Absence noonAbsence = absence(createPerson(), toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(calendar)
            .contains("VERSION:2.0")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE")
            .contains("X-MICROSOFT-CALSCALE:GREGORIAN")
            .contains("X-WR-CALNAME:Abwesenheitskalender")

            .contains("SUMMARY:Marlene Muster abwesend")
            .contains("DTSTART:20190526T120000Z")
            .contains("DTEND:20190526T160000Z");
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig);
    }
}
