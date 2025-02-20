package org.synyx.urlaubsverwaltung.application.application;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;

@Component
public class ApplicationReminderMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationService applicationService;
    private final SettingsService settingsService;
    private final ApplicationMailService applicationMailService;
    private final Clock clock;

    @Autowired
    ApplicationReminderMailService(ApplicationService applicationService, SettingsService settingsService, ApplicationMailService applicationMailService, Clock clock) {
        this.applicationService = applicationService;
        this.settingsService = settingsService;
        this.applicationMailService = applicationMailService;
        this.clock = clock;
    }

    public void sendWaitingApplicationsReminderNotification() {

        final boolean isRemindForWaitingApplicationsActive =
            settingsService.getSettings().getApplicationSettings().isRemindForWaitingApplications();

        if (isRemindForWaitingApplicationsActive) {
            final List<Application> longWaitingApplications = applicationService.getForStates(List.of(WAITING)).stream()
                .filter(isLongWaitingApplications())
                .toList();

            if (!longWaitingApplications.isEmpty()) {
                LOG.info("{} long waiting applications found. Sending Notification...", longWaitingApplications.size());

                applicationMailService.sendRemindForWaitingApplicationsReminderNotification(longWaitingApplications);

                for (Application longWaitingApplication : longWaitingApplications) {
                    longWaitingApplication.setRemindDate(LocalDate.now(clock));
                    applicationService.save(longWaitingApplication);
                }

                LOG.info("Sending Notification for waiting applications finished.");
            } else {
                LOG.info("No long waiting application found.");
            }
        }
    }

    public void sendUpcomingApplicationsReminderNotification() {

        final ApplicationSettings applicationSettings = settingsService.getSettings().getApplicationSettings();
        if (applicationSettings.isRemindForUpcomingApplications()) {
            final LocalDate today = LocalDate.now(clock);
            final LocalDate to = today.plusDays(applicationSettings.getDaysBeforeRemindForUpcomingApplications());
            final List<ApplicationStatus> allowedStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
            final List<Application> upcomingApplications = applicationService.getApplicationsWhereApplicantShouldBeNotifiedAboutUpcomingApplication(today, to, allowedStatuses);

            applicationMailService.sendRemindForUpcomingApplicationsReminderNotification(upcomingApplications);
            upcomingApplications.forEach(this::markUpcomingApplicationsReminderSent);
        }
    }

    private void markUpcomingApplicationsReminderSent(final Application application) {
        application.setUpcomingApplicationsReminderSend(LocalDate.now(clock));
        applicationService.save(application);
    }

    public void sendUpcomingHolidayReplacementReminderNotification() {

        final ApplicationSettings applicationSettings = settingsService.getSettings().getApplicationSettings();
        if (applicationSettings.isRemindForUpcomingHolidayReplacement()) {
            final LocalDate today = LocalDate.now(clock);
            final LocalDate to = today.plusDays(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement());
            final List<ApplicationStatus> allowedStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
            final List<Application> upcomingApplicationsForHolidayReplacement = applicationService.getApplicationsWhereHolidayReplacementShouldBeNotified(today, to, allowedStatuses);

            applicationMailService.sendRemindForUpcomingHolidayReplacement(upcomingApplicationsForHolidayReplacement);
            upcomingApplicationsForHolidayReplacement.forEach(this::markUpcomingHolidayReplacementReminderSent);
        }
    }

    private void markUpcomingHolidayReplacementReminderSent(final Application application) {
        application.setUpcomingHolidayReplacementNotificationSend(LocalDate.now(clock));
        applicationService.save(application);
    }

    private Predicate<Application> isLongWaitingApplications() {
        return application -> {

            final LocalDate remindDate = application.getRemindDate();
            if (remindDate == null) {
                Integer daysBeforeRemindForWaitingApplications =
                    settingsService.getSettings().getApplicationSettings().getDaysBeforeRemindForWaitingApplications();

                // never reminded before
                final LocalDate minDateForNotification = application.getApplicationDate().plusDays(daysBeforeRemindForWaitingApplications);

                // true -> remind!
                // false -> to early for notification
                return minDateForNotification.isBefore(LocalDate.now(clock));
            } else {
                // true -> not reminded today
                // false -> already reminded today
                return !remindDate.isEqual(LocalDate.now(clock));
            }
        };
    }
}
