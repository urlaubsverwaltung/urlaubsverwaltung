package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link TimeSettingsEntity}.
 */
class TimeSettingsEntityTest {

    @Test
    void ensureHasDefaultValues() {

        final TimeSettingsEntity timeSettings = new TimeSettingsEntity();

        assertThat(timeSettings.getTimeZoneId()).isEqualTo("Europe/Berlin");
        assertThat(timeSettings.getWorkDayBeginHour()).isEqualTo(8);
        assertThat(timeSettings.getWorkDayEndHour()).isEqualTo(16);
    }
}
