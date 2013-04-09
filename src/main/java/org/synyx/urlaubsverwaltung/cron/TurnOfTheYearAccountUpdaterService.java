
package org.synyx.urlaubsverwaltung.cron;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.scheduling.annotation.Scheduled;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;

import java.util.List;


/**
 * Is to be scheduled every turn of the year: calculates the remaining vacation days for the new year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class TurnOfTheYearAccountUpdaterService {

    private static final Logger LOG = Logger.getLogger("audit");

    private PersonService personService;
    private HolidaysAccountService accountService;
    private CalculationService calculationService;
    private MailService mailService;

    public TurnOfTheYearAccountUpdaterService(PersonService personService, HolidaysAccountService accountService,
        CalculationService calculationService, MailService mailService) {

        this.personService = personService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.mailService = mailService;
    }

    // executed every 1st January at 05:00 am
    @Scheduled(cron = "0 0 5 1 1 *")
    void updateHolidaysAccounts() {

        LOG.info("Starting updating users' accounts to calculate the remaining vacation days.");

        // what's the new year?
        int year = DateMidnight.now().getYear();

        // get all persons
        List<Person> persons = personService.getAllPersons();

        int counter = 0;

        StringBuilder builder = new StringBuilder();

        // get all their accounts and calculate the remaining vacation days for the new year
        for (Person p : persons) {
            LOG.info("Updating account of " + p.getLoginName());

            Account accountLastYear = accountService.getHolidaysAccount(year - 1, p);

            if (accountLastYear != null && accountLastYear.getAnnualVacationDays() != null) {
                BigDecimal leftDays = calculationService.calculateTotalLeftVacationDays(accountLastYear);

                Account accountNewYear = accountService.getOrCreateNewAccount(year, p);

                // setting new year's remaining vacation days to number of left days of the last year
                accountNewYear.setRemainingVacationDays(leftDays);
                LOG.info("Setting remaining vacation days of " + p.getLoginName() + " to " + leftDays + " for " + year);

                accountService.save(accountNewYear);

                counter++;

                builder.append(p.getFirstName()).append(" ").append(p.getLastName());
                builder.append(": ").append(leftDays).append("\n");
            }
        }

        LOG.info("Successfully updated holidays accounts: " + counter + "/" + persons.size());
        mailService.sendSuccessfullyUpdatedAccounts(builder.toString());
    }
}
