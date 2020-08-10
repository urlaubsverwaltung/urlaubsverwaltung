package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link AbsenceTimeConfiguration}.
 */
class AbsenceTimeConfigurationTest {

    private CalendarSettings calendarSettings;

    @BeforeEach
    void setUp() {
        calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(16);
    }

    @Test
    void ensureCorrectMorningStart() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
        assertThat(timeConfiguration.getMorningStart()).isEqualTo(8);
    }

    @Test
    void ensureCorrectMorningEnd() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
        assertThat(timeConfiguration.getMorningStart()).isEqualTo(12);
    }

    @Test
    void ensureCorrectNoonStart() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
        assertThat(timeConfiguration.getMorningStart()).isEqualTo(12);
    }

    @Test
    void ensureCorrectNoonEnd() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
        assertThat(timeConfiguration.getMorningStart()).isEqualTo(16);
    }
}
