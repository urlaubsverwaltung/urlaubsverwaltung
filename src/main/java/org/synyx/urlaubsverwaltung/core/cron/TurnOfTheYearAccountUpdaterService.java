
package org.synyx.urlaubsverwaltung.core.cron;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * Is to be scheduled every turn of the year: calculates the remaining vacation days for the new year.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class TurnOfTheYearAccountUpdaterService {

    private static final Logger LOG = Logger.getLogger(TurnOfTheYearAccountUpdaterService.class);

    private final PersonService personService;
    private final AccountService accountService;
    private final CalculationService calculationService;
    private final MailService mailService;

    @Autowired
    public TurnOfTheYearAccountUpdaterService(PersonService personService, AccountService accountService,
        CalculationService calculationService, MailService mailService) {

        this.personService = personService;
        this.accountService = accountService;
        this.calculationService = calculationService;
        this.mailService = mailService;
    }

    /**
     * This cronjob is executed every 1st January at 05:00 am, it calculates for every user how many vacation days
     * he/she has left from last year, this number is set as number of remaining vacation days for the new year.
     */
    @Scheduled(cron = "0 0 5 1 1 *")
    void updateHolidaysAccounts() {

        LOG.info("Starting updating users' accounts to calculate the remaining vacation days.");

        // what's the new year?
        int year = DateMidnight.now().getYear();

        // get all persons
        List<Person> persons = personService.getActivePersons();

        List<Account> updatedAccounts = new ArrayList<>();

        // get all their accounts and calculate the remaining vacation days for the new year
        for (Person person : persons) {
            LOG.info("Updating account of " + person.getLoginName());

            Account accountLastYear = accountService.getHolidaysAccount(year - 1, person);

            if (accountLastYear != null && accountLastYear.getAnnualVacationDays() != null) {
                BigDecimal leftDays = calculationService.calculateTotalLeftVacationDays(accountLastYear);

                Account holidaysAccount = accountService.createHolidaysAccount(person, DateUtil.getFirstDayOfYear(year),
                        DateUtil.getLastDayOfYear(year), accountLastYear.getAnnualVacationDays(), leftDays,
                        BigDecimal.ZERO);

                LOG.info("Setting remaining vacation days of " + person.getLoginName() + " to " + leftDays + " for "
                    + year);

                updatedAccounts.add(holidaysAccount);
            }
        }

        LOG.info("Successfully updated holidays accounts: " + updatedAccounts.size() + "/" + persons.size());
        mailService.sendSuccessfullyUpdatedAccounts(updatedAccounts);
    }
}
