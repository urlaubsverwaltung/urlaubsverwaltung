/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.util.DateService;


/**
 * Class which manages actions at certain dates
 *
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
        // 1. Januar: verbliebener Urlaub wird auf Resturlaub addiert
        if (day == 1 && month == 0) {
            personService.updateVacationDays();
        }

        // 1. April: Resturlaub verfaellt, wird auf 0 gesetzt
        if (day == 1 && month == 3) {
            personService.deleteResturlaub();
        }

        // Montag hat Nummer 1, Sonntag 0
        // jeden Montag Rundmail, wer diese Woche Urlaub hat
        if (dateService.getDayOfWeek() == 1 && dateService.getTime() == 4) {
            // fehlt noch: Rundmail jeden Montag, wer frei hat
        }

        // Erinnerungsmail, dass Resturlaub bald verfaellt
        // Idee war: 2.1., 2.2., 2.3., 14.3.
        if ((day == 2 && month == 0) || (day == 2 && month == 1) || (day == 2 && month == 2)
                || (day == 14 && month == 2)) {
        }
    }

//    kp...muss noch irgendwie gemacht werden
//    public void checkWho() {
//        personService.getAllUrlauberForThisWeekAndPutItInAnEmail(null, null);
//    }
}
