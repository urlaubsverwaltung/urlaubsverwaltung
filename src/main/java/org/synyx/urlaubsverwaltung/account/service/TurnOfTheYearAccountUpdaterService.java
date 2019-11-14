package org.synyx.urlaubsverwaltung.account.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;


/**
 * Is to be scheduled every turn of the year: calculates the remaining vacation days for the new year.
 */
@Service
public class TurnOfTheYearAccountUpdaterService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final MailService mailService;

    @Autowired
    public TurnOfTheYearAccountUpdaterService(PersonService personService, AccountService accountService,
                                              AccountInteractionService accountInteractionService, MailService mailService) {

        this.personService = personService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.mailService = mailService;
    }

    void updateAccountsForNextPeriod() {

        LOG.info("Starting update of holidays accounts to calculate the remaining vacation days.");

        // what's the new year?
        final int year = ZonedDateTime.now(UTC).getYear();

        // get all persons
        final List<Person> persons = personService.getActivePersons();

        // get all their accounts and calculate the remaining vacation days for the new year
        final List<Account> updatedAccounts = new ArrayList<>();
        for (Person person : persons) {
            LOG.info("Updating account of person with id {}", person.getId());

            final Optional<Account> accountLastYear = accountService.getHolidaysAccount(year - 1, person);

            if (accountLastYear.isPresent() && accountLastYear.get().getAnnualVacationDays() != null) {
                final Account holidaysAccount = accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(
                    accountLastYear.get());

                LOG.info("Setting remaining vacation days of person with id {} to {} for {}",
                    person.getId(), holidaysAccount.getRemainingVacationDays(), year);

                updatedAccounts.add(holidaysAccount);
            }
        }

        LOG.info("Successfully updated holidays accounts: {} / {}", updatedAccounts.size(), persons.size());
        sendSuccessfullyUpdatedAccountsNotification(updatedAccounts);
    }

    /**
     * Sends mail to the tool's manager if holidays accounts were updated successfully on 1st January of a year.
     * (setting remaining vacation days)
     *
     * @param updatedAccounts that have been successfully updated
     */
    private void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts) {

        Map<String, Object> model = new HashMap<>();
        model.put("accounts", updatedAccounts);
        model.put("today", LocalDate.now(UTC));

        final String subjectMessageKey = "subject.account.updatedRemainingDays";
        final String templateName = "updated_accounts";

        // send email to office for printing statistic
        mailService.sendMailTo(NOTIFICATION_OFFICE, subjectMessageKey, templateName, model);

        // send email to manager to notify about update of accounts
        mailService.sendTechnicalMail(subjectMessageKey, templateName, model);
    }
}
