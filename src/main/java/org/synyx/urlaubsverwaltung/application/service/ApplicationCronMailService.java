package org.synyx.urlaubsverwaltung.application.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
class ApplicationCronMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationService applicationService;
    private final SettingsService settingsService;
    private final ApplicationMailService applicationMailService;
    private final Clock clock;

    @Autowired
    ApplicationCronMailService(ApplicationService applicationService, SettingsService settingsService, ApplicationMailService applicationMailService, Clock clock) {
        this.applicationService = applicationService;
        this.settingsService = settingsService;
        this.applicationMailService = applicationMailService;
        this.clock = clock;
    }

    void sendWaitingApplicationsReminderNotification() {

        boolean isRemindForWaitingApplicationsActive =
            settingsService.getSettings().getAbsenceSettings().isRemindForWaitingApplications();

        if (isRemindForWaitingApplicationsActive) {
            List<Application> allWaitingApplications =
                applicationService.getApplicationsForACertainState(ApplicationStatus.WAITING);

            List<Application> longWaitingApplications = allWaitingApplications.stream()
                .filter(isLongWaitingApplications())
                .collect(Collectors.toList());

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

    private Predicate<Application> isLongWaitingApplications() {
        return application -> {

            LocalDate remindDate = application.getRemindDate();
            if (remindDate == null) {
                Integer daysBeforeRemindForWaitingApplications =
                    settingsService.getSettings().getAbsenceSettings().getDaysBeforeRemindForWaitingApplications();

                // never reminded before
                LocalDate minDateForNotification = application.getApplicationDate()
                    .plusDays(daysBeforeRemindForWaitingApplications);

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
