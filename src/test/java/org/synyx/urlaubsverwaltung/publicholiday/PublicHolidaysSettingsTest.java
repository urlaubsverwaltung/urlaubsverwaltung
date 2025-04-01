package org.synyx.urlaubsverwaltung.publicholiday;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;

class PublicHolidaysSettingsTest {

    @Test
    void ensureDefaultValues() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        assertThat(settings.getWorkingDurationForChristmasEve()).isEqualTo(MORNING);
        assertThat(settings.getWorkingDurationForNewYearsEve()).isEqualTo(MORNING);
    }
}
