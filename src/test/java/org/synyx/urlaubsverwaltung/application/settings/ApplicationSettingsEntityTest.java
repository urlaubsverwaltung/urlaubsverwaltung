package org.synyx.urlaubsverwaltung.application.settings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ApplicationSettingsEntityTest {

    @Test
    void ensureDefaultValues() {

        final ApplicationSettingsEntity settings = new ApplicationSettingsEntity();
        Assertions.assertThat(settings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(12);
        Assertions.assertThat(settings.isRemindForUpcomingApplications()).isFalse();
        Assertions.assertThat(settings.getDaysBeforeRemindForUpcomingApplications()).isEqualTo(3);
    }
}
