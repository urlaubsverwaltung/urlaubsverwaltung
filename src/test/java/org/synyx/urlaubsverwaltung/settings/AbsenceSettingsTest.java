package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AbsenceSettingsTest {

    @Test
    void ensureDefaultValues() {

        AbsenceSettings settings = new AbsenceSettings();

        assertThat(settings.getMaximumAnnualVacationDays()).isNotNull();
        assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isNotNull();
        assertThat(settings.getMaximumSickPayDays()).isNotNull();
        assertThat(settings.getDaysBeforeEndOfSickPayNotification()).isNotNull();

        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
        assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(12);
        assertThat(settings.getMaximumSickPayDays()).isEqualTo(42);
        assertThat(settings.getDaysBeforeEndOfSickPayNotification()).isEqualTo(7);
    }
}
