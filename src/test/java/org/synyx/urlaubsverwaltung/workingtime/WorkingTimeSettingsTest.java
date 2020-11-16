package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimeSettingsTest {

    @Test
    void ensureDefaultValues() {

        final WorkingTimeSettings settings = new WorkingTimeSettings();
        assertThat(settings.getWorkingDurationForChristmasEve()).isEqualTo(DayLength.MORNING);
        assertThat(settings.getWorkingDurationForNewYearsEve()).isEqualTo(DayLength.MORNING);
        assertThat(settings.getFederalState()).isEqualTo(FederalState.BADEN_WUERTTEMBERG);
    }
}
