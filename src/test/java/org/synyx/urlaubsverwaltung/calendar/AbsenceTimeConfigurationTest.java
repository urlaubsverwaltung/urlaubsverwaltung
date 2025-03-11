package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link CalendarAbsenceConfiguration}.
 */
class AbsenceTimeConfigurationTest {

    private TimeSettings timeSettings;

    @BeforeEach
    void setUp() {
        timeSettings = new TimeSettings();
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayBeginMinute(15);
        timeSettings.setWorkDayEndHour(16);
        timeSettings.setWorkDayEndMinute(30);
    }

    @Test
    void ensureCorrectMorningStartTime() {
        final CalendarAbsenceConfiguration timeConfiguration = new CalendarAbsenceConfiguration(timeSettings);
        assertThat(timeConfiguration.morningStartTime()).isEqualTo(LocalTime.of(8,15));
    }

    @Test
    void ensureCorrectMorningEndTime() {
        final CalendarAbsenceConfiguration timeConfiguration = new CalendarAbsenceConfiguration(timeSettings);
        assertThat(timeConfiguration.morningEndTime()).isEqualTo(LocalTime.of(12,22, 30));
    }

    @Test
    void ensureCorrectNoonStartTime() {
        final CalendarAbsenceConfiguration timeConfiguration = new CalendarAbsenceConfiguration(timeSettings);
        assertThat(timeConfiguration.noonStartTime()).isEqualTo(LocalTime.of(12,22, 30));
    }

    @Test
    void ensureCorrectNoonEndTime() {
        final CalendarAbsenceConfiguration timeConfiguration = new CalendarAbsenceConfiguration(timeSettings);
        assertThat(timeConfiguration.noonEndTime()).isEqualTo(LocalTime.of(16,30));
    }
}
