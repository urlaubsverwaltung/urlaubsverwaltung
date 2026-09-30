package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.synyx.urlaubsverwaltung.QueryCountTestSupport;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * Guards the application reminder jobs against N+1 queries: the number of statements must not depend on the number
 * of applications. Today is 2026-05-04 (see {@link QueryCountTestSupport}).
 */
class ApplicationReminderJobsQueryCountIT extends QueryCountTestSupport {

    /**
     * Deciding whether a waiting application is due for a reminder reads the settings once per application (see #6672).
     */
    private static final int STATEMENTS_PER_WAITING_APPLICATION = 1;

    @Autowired
    private ApplicationReminderMailService applicationReminderMailService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;
    @Autowired
    private SettingsService settingsService;

    @BeforeEach
    void enableReminders() {
        final Settings settings = settingsService.getSettings();
        settings.getApplicationSettings().setRemindForWaitingApplications(true);
        settings.getApplicationSettings().setDaysBeforeRemindForWaitingApplications(2);
        settings.getApplicationSettings().setRemindForUpcomingApplications(true);
        settings.getApplicationSettings().setDaysBeforeRemindForUpcomingApplications(3);
        settings.getApplicationSettings().setRemindForUpcomingHolidayReplacement(true);
        settings.getApplicationSettings().setDaysBeforeRemindForUpcomingHolidayReplacement(3);
        settingsService.save(settings);
    }

    @Test
    void sendWaitingApplicationsReminderNotificationDoesNotGrowWithApplications() {
        final Measurement small = seedAndMeasure(1, applicationReminderMailService::sendWaitingApplicationsReminderNotification);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, applicationReminderMailService::sendWaitingApplicationsReminderNotification);

        // 3 departments with 3 additional members, each with one waiting application
        final int additionalApplications = 9;

        // one mail per manager listing all applications, so the number of mails does not grow
        assertThat(small.mails()).isPositive();
        assertThat(large.statements()).isLessThanOrEqualTo(small.statements() + STATEMENTS_PER_WAITING_APPLICATION * additionalApplications);
    }

    @Test
    void sendUpcomingApplicationsReminderNotificationDoesNotGrowWithApplications() {
        final Measurement small = seedAndMeasure(1, applicationReminderMailService::sendUpcomingApplicationsReminderNotification);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, applicationReminderMailService::sendUpcomingApplicationsReminderNotification);

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isEqualTo(small.statements());
    }

    @Test
    void sendUpcomingHolidayReplacementReminderNotificationDoesNotGrowWithApplications() {
        final Measurement small = seedAndMeasure(1, applicationReminderMailService::sendUpcomingHolidayReplacementReminderNotification);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, applicationReminderMailService::sendUpcomingHolidayReplacementReminderNotification);

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isEqualTo(small.statements());
    }

    private Measurement seedAndMeasure(int membersPerDepartment, Runnable job) {

        final Staff staff = createStaff(membersPerDepartment);
        final List<Person> members = staff.members();

        final VacationType<?> holiday = vacationTypeService.getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.isOfCategory(HOLIDAY))
            .findFirst()
            .orElseThrow();

        for (int i = 0; i < members.size(); i++) {
            final Person member = members.get(i);
            final Person replacement = members.get((i + 1) % members.size());

            final Application waiting = new Application();
            waiting.setPerson(member);
            waiting.setApplier(member);
            waiting.setVacationType(holiday);
            waiting.setDayLength(FULL);
            waiting.setStartDate(LocalDate.of(2026, 6, 1));
            waiting.setEndDate(LocalDate.of(2026, 6, 1));
            waiting.setApplicationDate(LocalDate.of(2026, 4, 1));
            waiting.setStatus(WAITING);
            waiting.setTwoStageApproval(true);
            applicationService.save(waiting);

            final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
            holidayReplacement.setPerson(replacement);
            holidayReplacement.setNote("please take over");

            final Application upcoming = new Application();
            upcoming.setPerson(member);
            upcoming.setApplier(member);
            upcoming.setBoss(staff.boss());
            upcoming.setVacationType(holiday);
            upcoming.setDayLength(FULL);
            upcoming.setStartDate(LocalDate.of(2026, 5, 6));
            upcoming.setEndDate(LocalDate.of(2026, 5, 6));
            upcoming.setApplicationDate(LocalDate.of(2026, 4, 1));
            upcoming.setStatus(ALLOWED);
            upcoming.setHolidayReplacements(new ArrayList<>(List.of(holidayReplacement)));
            applicationService.save(upcoming);
        }

        return measure(job);
    }
}
