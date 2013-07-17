
package org.synyx.urlaubsverwaltung.cron;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.person.PersonService;


/**
 * Service to send emails at a particular time. (Cronjob)
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CronMailService {

    private PersonService personService;

    public CronMailService(PersonService personService) {

        this.personService = personService;
    }

    // TODO: remove comment if to be executed
    // executed every monday at 06:00 am
//    @Scheduled(cron = "0 0 6 * * MON")
    void sendWeeklyVacationForecast() {

        personService.getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(new DateMidnight(2012, 11, 26),
            new DateMidnight(2012, 12, 2));
    }
}
