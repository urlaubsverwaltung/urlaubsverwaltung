package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.AbsenceSettings;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceSettingsTest {

    @Test
    void ensureDefaultValues() {

        final AbsenceSettings settings = new AbsenceSettings();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
        assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(12);
    }
}
