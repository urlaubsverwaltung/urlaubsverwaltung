
package org.synyx.urlaubsverwaltung.cron;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.util.List;


/**
 * Service to send emails at a particular time. (Cronjob)
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class CronMailService {

    private SickNoteService sickNoteService;
    private MailService mailService;

    @Autowired
    public CronMailService(SickNoteService sickNoteService, MailService mailService) {

        this.sickNoteService = sickNoteService;
        this.mailService = mailService;
    }

    // executed every day at 06:00 am
    @Scheduled(cron = "0 0 6 * * ?")
    void sendEndOfSickPayNotification() {

        List<SickNote> sickNotes = sickNoteService.getSickNotesReachingEndOfSickPay();

        System.out.println(sickNotes.size());

        for (SickNote sickNote : sickNotes) {
            mailService.sendEndOfSickPayNotification(sickNote);
        }
    }
}
