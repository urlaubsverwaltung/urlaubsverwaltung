
package org.synyx.urlaubsverwaltung.core.cron;

import org.springframework.beans.factory.annotation.Autowired;

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
    public CronMailService(ApplicationService applicationService, SettingsService settingsService, SickNoteService sickNoteService, MailService mailService) {

        this.applicationService = applicationService;
        this.settingsService = settingsService;
        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "${uv.cron.endOfSickPayNotification}")
    void sendEndOfSickPayNotification() {

        List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        for (SickNote sickNote : sickNotes) {
            mailService.sendEndOfSickPayNotification(sickNote);
        }
    }

    @Scheduled(cron = "${uv.cron.remindForNotification}")
    void sendWaitingApplicationsReminderNotification() {

        boolean isRemindForWaitingApplicationsActive = settingsService.getSettings().getAbsenceSettings()
                .getRemindForWaitingApplications();

        if (isRemindForWaitingApplicationsActive) {
            List<Application> waitingApplications = applicationService.getApplicationsForACertainState(ApplicationStatus.WAITING);

            mailService.sendRemindForWaitingApplicationsReminderNotification(waitingApplications);
        }

    }
}
