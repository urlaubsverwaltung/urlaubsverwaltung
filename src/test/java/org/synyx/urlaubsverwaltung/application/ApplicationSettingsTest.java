package org.synyx.urlaubsverwaltung.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSettingsTest {

    @Test
    void ensureDefaultValues() {

        final ApplicationSettings settings = new ApplicationSettings();
        assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(12);
        assertThat(settings.isRemindForUpcomingApplications()).isFalse();
        assertThat(settings.getDaysBeforeRemindForUpcomingApplications()).isEqualTo(3);
    }
}
