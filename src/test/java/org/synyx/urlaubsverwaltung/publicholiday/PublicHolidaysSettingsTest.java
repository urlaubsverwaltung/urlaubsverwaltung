package org.synyx.urlaubsverwaltung.publicholiday;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

class PublicHolidaysSettingsTest {

    @Test
    void ensureDefaultValues() {

        final PublicHolidaysSettings settings = new PublicHolidaysSettings();
        assertThat(settings.getFederalState()).isEqualTo(GERMANY_BADEN_WUERTTEMBERG);
    }
}
