package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AbsenceTimeConfiguration}.
 */
class AbsenceTimeConfigurationTest {

    private TimeSettings timeSettings;

    @BeforeEach
    void setUp() {
        timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayEndHour(16);
    }

    @Test
    void ensureCorrectMorningStart() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(timeSettings);
        assertThat(timeConfiguration.getMorningStart()).isEqualTo(8);
    }

    @Test
    void ensureCorrectMorningEnd() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(timeSettings);
        assertThat(timeConfiguration.getMorningEnd()).isEqualTo(12);
    }

    @Test
    void ensureCorrectNoonStart() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(timeSettings);
        assertThat(timeConfiguration.getNoonStart()).isEqualTo(12);
    }

    @Test
    void ensureCorrectNoonEnd() {
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(timeSettings);
        assertThat(timeConfiguration.getNoonEnd()).isEqualTo(16);
    }
}
