package org.synyx.urlaubsverwaltung.account;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailAttachment;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL;

/**
 * Is to be scheduled every turn of the year: calculates the remaining vacation days for the new year.
 */
@Service
public class TurnOfTheYearAccountUpdaterService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final VacationDaysReminderService vacationDaysReminderService;
    private final MailService mailService;
    private final MailRecipientService mailRecipientService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;
    private final RemainingVacationDaysCsvExportService remainingVacationDaysCsvExportService;
    private final Clock clock;

    @Autowired
    TurnOfTheYearAccountUpdaterService(
        PersonService personService, AccountService accountService,
        AccountInteractionService accountInteractionService,
        VacationDaysReminderService vacationDaysReminderService,
        MailService mailService, MailRecipientService mailRecipientService,
        PersonBasedataService personBasedataService, DepartmentService departmentService,
        RemainingVacationDaysCsvExportService remainingVacationDaysCsvExportService, Clock clock
    ) {
        this.personService = personService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.vacationDaysReminderService = vacationDaysReminderService;
        this.mailService = mailService;
        this.mailRecipientService = mailRecipientService;
        this.personBasedataService = personBasedataService;
        this.departmentService = departmentService;
        this.remainingVacationDaysCsvExportService = remainingVacationDaysCsvExportService;
        this.clock = clock;
    }

    public void updateAccountsForNextPeriod() {

        LOG.info("Starting update of holidays accounts to calculate the remaining vacation days.");

        final int year = Year.now(clock).getValue();
        final List<Person> activePersons = personService.getActivePersons();

        // load all last year's accounts with a single query instead of one query per person
        final Map<Person, Account> lastYearAccountByPerson = accountService.getHolidaysAccount(year - 1, activePersons).stream()
            .collect(toMap(Account::getPerson, identity(), (first, second) -> first));

        // get all their accounts and calculate the remaining vacation days for the new year
        final List<Account> updatedAccounts = new ArrayList<>();
        for (Person person : activePersons) {
            final Optional<Account> accountLastYear = Optional.ofNullable(lastYearAccountByPerson.get(person));
            if (accountLastYear.isPresent() && accountLastYear.get().getAnnualVacationDays() != null) {
                LOG.info("Updating account of person with id {}", person.getId());
                final Account holidaysAccount = accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(accountLastYear.get());
                LOG.info("Setting remaining vacation days of person with id {} to {} for {}", person.getId(), holidaysAccount.getRemainingVacationDays(), year);
                updatedAccounts.add(holidaysAccount);
            } else {
                LOG.info("No holiday account updated for person with id {}. Reason: No account for last year or annual vacation days not defined.", person.getId());
            }
        }

        LOG.info("Updated holidays accounts for year {}: {} / {} ({} persons have no account)",
            year,
            updatedAccounts.size(),
            activePersons.size(),
            activePersons.size() - updatedAccounts.size());
        sendSuccessfullyUpdatedAccountsNotification(updatedAccounts);
        vacationDaysReminderService.remindForRemainingVacationDays();
    }

    /**
     * Sends mail to the tool's manager if holidays accounts were updated successfully on 1st January of a year.
     * (setting remaining vacation days)
     *
     * @param updatedAccounts that have been successfully updated
     */
    private void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts) {

        final List<Person> recipients = mailRecipientService.getRecipientsWith(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL);
        if (recipients.isEmpty()) {
            return;
        }

        final Map<String, Object> model = Map.of(
            "accounts", updatedAccounts,
            "totalRemainingVacationDays", updatedAccounts.stream().map(Account::getRemainingVacationDays).reduce(BigDecimal::add).orElse(BigDecimal.ZERO),
            "today", LocalDate.now(clock)
        );
        final String subjectMessageKey = "subject.account.updatedRemainingDays";
        final String templateName = "account_cron_updated_accounts_turn_of_the_year";

        final List<RemainingVacationDaysCsvRow> csvRows = toCsvRows(updatedAccounts);
        final Year year = Year.now(clock);
        final FilterPeriod newYear = new FilterPeriod(year.atDay(1), year.atMonth(12).atEndOfMonth());

        // send email to office for printing statistic
        final Mail mailToOffice = Mail.builder()
            .withRecipient(recipients)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, _ -> model)
            .withAttachment(locale -> {
                final CSVFile csvFile = remainingVacationDaysCsvExportService.generateCSV(newYear, locale, csvRows);
                return new MailAttachment(csvFile.fileName(), csvFile.resource());
            })
            .build();
        mailService.send(mailToOffice);
    }

    private List<RemainingVacationDaysCsvRow> toCsvRows(List<Account> accounts) {

        final List<Person> persons = accounts.stream().map(Account::getPerson).toList();
        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(persons.stream().map(Person::getId).toList());
        final Map<PersonId, List<String>> departmentNamesByPersonId = departmentService.getDepartmentNamesByMembers(persons);

        return accounts.stream()
            .map(account -> {
                final PersonId personId = account.getPerson().getIdAsPersonId();
                return RemainingVacationDaysCsvRow.of(account, basedataByPersonId.get(personId), departmentNamesByPersonId.get(personId));
            })
            .toList();
    }
}
