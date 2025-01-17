package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSettingsDTOTest {

    @Test
    void happyPathApplicationSettingsToDTO() {
        ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(6);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(3);
        applicationSettings.setRemindForWaitingApplications(true);
        applicationSettings.setAllowHalfDays(true);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(5);
        applicationSettings.setRemindForUpcomingApplications(true);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(10);
        applicationSettings.setRemindForUpcomingHolidayReplacement(true);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(15);

        ApplicationSettingsDTO applicationSettingsDTO = ApplicationSettingsDTO.of(applicationSettings);

        assertThat(applicationSettingsDTO.maximumMonthsToApplyForLeaveInAdvance()).isEqualTo(applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance());
        assertThat(applicationSettingsDTO.maximumMonthsToApplyForLeaveAfterwards()).isEqualTo(applicationSettings.getMaximumMonthsToApplyForLeaveAfterwards());
        assertThat(applicationSettingsDTO.remindForWaitingApplications()).isEqualTo(applicationSettings.isRemindForWaitingApplications());
        assertThat(applicationSettingsDTO.allowHalfDays()).isEqualTo(applicationSettings.isAllowHalfDays());
        assertThat(applicationSettingsDTO.daysBeforeRemindForWaitingApplications()).isEqualTo(applicationSettings.getDaysBeforeRemindForWaitingApplications());
        assertThat(applicationSettingsDTO.remindForUpcomingApplications()).isEqualTo(applicationSettings.isRemindForUpcomingApplications());
        assertThat(applicationSettingsDTO.daysBeforeRemindForUpcomingApplications()).isEqualTo(applicationSettings.getDaysBeforeRemindForUpcomingApplications());
        assertThat(applicationSettingsDTO.remindForUpcomingHolidayReplacement()).isEqualTo(applicationSettings.isRemindForUpcomingHolidayReplacement());
        assertThat(applicationSettingsDTO.daysBeforeRemindForUpcomingHolidayReplacement()).isEqualTo(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement());
    }

    @Test
    void happyPathDTOToApplicationSettings() {
        ApplicationSettingsDTO applicationSettingsDTO = new ApplicationSettingsDTO(
            6, 3, true, true, 5, true, 10, true, 15
        );

        ApplicationSettings applicationSettings = applicationSettingsDTO.toApplicationSettings();

        assertThat(applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(applicationSettingsDTO.maximumMonthsToApplyForLeaveInAdvance());
        assertThat(applicationSettings.getMaximumMonthsToApplyForLeaveAfterwards()).isEqualTo(applicationSettingsDTO.maximumMonthsToApplyForLeaveAfterwards());
        assertThat(applicationSettings.isRemindForWaitingApplications()).isEqualTo(applicationSettingsDTO.remindForWaitingApplications());
        assertThat(applicationSettings.isAllowHalfDays()).isEqualTo(applicationSettingsDTO.allowHalfDays());
        assertThat(applicationSettings.getDaysBeforeRemindForWaitingApplications()).isEqualTo(applicationSettingsDTO.daysBeforeRemindForWaitingApplications());
        assertThat(applicationSettings.isRemindForUpcomingApplications()).isEqualTo(applicationSettingsDTO.remindForUpcomingApplications());
        assertThat(applicationSettings.getDaysBeforeRemindForUpcomingApplications()).isEqualTo(applicationSettingsDTO.daysBeforeRemindForUpcomingApplications());
        assertThat(applicationSettings.isRemindForUpcomingHolidayReplacement()).isEqualTo(applicationSettingsDTO.remindForUpcomingHolidayReplacement());
        assertThat(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement()).isEqualTo(applicationSettingsDTO.daysBeforeRemindForUpcomingHolidayReplacement());
    }

}
