/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.util.DateService;


/**
 * @author  aljona
 */
public class DateEventsServiceImpl {

    private DateService dateService;
    private PersonService personService;
    private MailService mailService;

    public DateEventsServiceImpl(DateService dateService, PersonService personService, MailService mailService) {

        this.dateService = dateService;
        this.personService = personService;
        this.mailService = mailService;
    }

    public void dateEvents() {

        int day = dateService.getDay();
        int month = dateService.getMonth();

        // 1. Monatstag == 1, 1. Monate == 0
        if (day == 1 && month == 0) {
            personService.updateVacationDays();
        }

        if (day == 1 && month == 3) {
            personService.deleteResturlaub();
        }

        // Montag hat Nummer 1, Sonntag 0
        if (dateService.getDayOfWeek() == 1 && dateService.getTime() == 4) {
            // fehlt noch: Rundmail jeden Montag, wer frei hat
        }

        if ((day == 2 && month == 0) || (day == 2 && month == 1) || (day == 2 && month == 2)
                || (day == 14 && month == 2)) {
            mailService.sendDecayNotification();
        }
    }

//    kp...muss noch irgendwie gemacht werden
//    public void checkWho() {
//        personService.getAllUrlauberForThisWeekAndPutItInAnEmail(null, null);
//    }
}
