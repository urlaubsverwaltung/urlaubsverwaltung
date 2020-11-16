package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link TimeSettings}.
 */
class TimeSettingsTest {

    @Test
    void ensureHasDefaultValues() {

        final TimeSettings timeSettings = new TimeSettings();

        assertThat(timeSettings.getTimeZoneId()).isEqualTo("Europe/Berlin");
        assertThat(timeSettings.getWorkDayBeginHour()).isEqualTo(8);
        assertThat(timeSettings.getWorkDayEndHour()).isEqualTo(16);
    }
}
