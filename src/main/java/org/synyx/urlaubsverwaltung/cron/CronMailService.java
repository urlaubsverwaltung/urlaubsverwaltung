
package org.synyx.urlaubsverwaltung.cron;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;


/**
 * Service to send emails at a particular time. (Cronjob)
 */
@Service
public class CronMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

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

    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     */
    @Scheduled(cron = "${uv.cron.endOfSickPayNotification}")
    public void sendEndOfSickPayNotification() {

        List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        LOG.info("Found {} sick notes reaching end of sick pay", sickNotes.size());

        final String subjectMessageKey = "subject.sicknote.endOfSickPay";
        final String templateName = "sicknote_end_of_sick_pay";
        final Integer maximumSickPayDays = settingsService.getSettings().getAbsenceSettings().getMaximumSickPayDays();

        for (SickNote sickNote : sickNotes) {

            Map<String, Object> model = new HashMap<>();
            model.put("maximumSickPayDays", maximumSickPayDays);
            model.put("sickNote", sickNote);

            mailService.sendMailTo(sickNote.getPerson(), subjectMessageKey, templateName, model);
            mailService.sendMailTo(NOTIFICATION_OFFICE, subjectMessageKey, templateName, model);
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

            if (!longWaitingApplications.isEmpty()) {
                LOG.info("{} long waiting applications found. Sending Notification...", longWaitingApplications.size());

                // applicationService.sendRemindForWaitingApplicationsReminderNotification(longWaitingApplications);

                for (Application longWaitingApplication : longWaitingApplications) {
                    longWaitingApplication.setRemindDate(LocalDate.now(UTC));
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
                return minDateForNotification.isBefore(LocalDate.now(UTC));
            } else {
                // true -> not reminded today
                // false -> already reminded today
                return !remindDate.isEqual(LocalDate.now(UTC));
            }
        };
    }
}
