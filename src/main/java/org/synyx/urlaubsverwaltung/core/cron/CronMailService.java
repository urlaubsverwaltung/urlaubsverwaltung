
package org.synyx.urlaubsverwaltung.core.cron;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Service to send emails at a particular time. (Cronjob)
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class CronMailService {

    private final ApplicationService applicationService;
    private final SettingsService settingsService;
    private final SickNoteService sickNoteService;
    private final MailService mailService;

    @Autowired
    public CronMailService(ApplicationService applicationService,
                           SettingsService settingsService,
                           SickNoteService sickNoteService,
                           MailService mailService) {

        this.applicationService = applicationService;
        this.settingsService = settingsService;
        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "${uv.cron.endOfSickPayNotification}")
    public void sendEndOfSickPayNotification() {

        List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        for (SickNote sickNote : sickNotes) {
            mailService.sendEndOfSickPayNotification(sickNote);
        }
    }

    @Scheduled(cron = "${uv.cron.daysBeforeWaitingApplicationsReminderNotification}")
    public void sendWaitingApplicationsReminderNotification() {

        boolean isRemindForWaitingApplicationsActive =
                settingsService.getSettings().getAbsenceSettings().getRemindForWaitingApplications();

        if (isRemindForWaitingApplicationsActive) {
            List<Application> allWaitingApplications =
                    applicationService.getApplicationsForACertainState(ApplicationStatus.WAITING);

            List<Application> longWaitingApplications = allWaitingApplications.stream()
                    .filter(isLongWaitingApplications())
                    .collect(Collectors.toList());

            mailService.sendRemindForWaitingApplicationsReminderNotification(longWaitingApplications);

            for (Application longWaitingApplication : longWaitingApplications) {
                longWaitingApplication.setRemindDate(DateMidnight.now());
                applicationService.save(longWaitingApplication);
            }

        }

    }

    private Predicate<Application> isLongWaitingApplications() {
        return application -> {

            DateMidnight remindDate = application.getRemindDate();
            if (remindDate == null) {
                Integer daysBeforeRemindForWaitingApplications =
                        settingsService.getSettings().getAbsenceSettings().getDaysBeforeRemindForWaitingApplications();

                // never reminded before
                DateMidnight minDateForNotification = application.getApplicationDate()
                        .plusDays(daysBeforeRemindForWaitingApplications);

                // true -> remind!
                // false -> to early for notification
                return minDateForNotification.isBeforeNow();
            } else {
                // true -> not reminded today
                // false -> allready remined today
                return !remindDate.isEqual(DateMidnight.now());
            }
        };
    }
}
