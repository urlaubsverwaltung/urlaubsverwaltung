
package org.synyx.urlaubsverwaltung.core.cron;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
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

    private final SickNoteService sickNoteService;
    private final MailService mailService;

    @Autowired
    public CronMailService(SickNoteService sickNoteService, MailService mailService) {

        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
    }

    // executed every day at 06:00 am
    @Scheduled(cron = "0 0 6 * * ?")
    void sendEndOfSickPayNotification() {

        List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        for (SickNote sickNote : sickNotes) {
            mailService.sendEndOfSickPayNotification(sickNote);
        }
    }
}
