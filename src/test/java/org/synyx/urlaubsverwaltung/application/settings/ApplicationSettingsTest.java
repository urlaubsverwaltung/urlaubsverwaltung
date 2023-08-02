package org.synyx.urlaubsverwaltung.application.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSettingsTest {

    @Test
    void ensureDefaultValues() {
        final ApplicationSettings settings = new ApplicationSettings();
        assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(18);
        assertThat(settings.getMaximumMonthsToApplyForLeaveAfterwards()).isEqualTo(12);
        assertThat(settings.isRemindForWaitingApplications()).isFalse();
        assertThat(settings.isAllowHalfDays()).isTrue();
        assertThat(settings.getDaysBeforeRemindForWaitingApplications()).isEqualTo(2);
        assertThat(settings.isRemindForUpcomingApplications()).isFalse();
        assertThat(settings.getDaysBeforeRemindForUpcomingApplications()).isEqualTo(3);
        assertThat(settings.isRemindForUpcomingHolidayReplacement()).isFalse();
        assertThat(settings.getDaysBeforeRemindForUpcomingHolidayReplacement()).isEqualTo(3);
    }
}
