package org.synyx.urlaubsverwaltung.account;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class VacationDaysReminderService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final MailService mailService;
    private final Clock clock;

    @Autowired
    VacationDaysReminderService(PersonService personService, AccountService accountService, VacationDaysService vacationDaysService, WorkingTimeCalendarService workingTimeCalendarService,
                                MailService mailService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.mailService = mailService;
        this.clock = clock;
    }

    /**
     * Reminds for vacation days left for <b>current year</b>.
     */
    @Async
    public void remindForCurrentlyLeftVacationDays() {
        final Year year = Year.now(clock);
        final List<Person> persons = personService.getActivePersons();

        accountService.getHolidaysAccount(year.getValue(), persons).stream()
            .filter(Account::doRemainingVacationDaysExpire)
            .forEach(account -> {
                final BigDecimal vacationDaysLeft = vacationDaysService.getTotalLeftVacationDays(account);
                if (vacationDaysLeft.compareTo(ZERO) > 0) {
                    sendReminderForCurrentlyLeftVacationDays(account.getPerson(), vacationDaysLeft, year.plusYears(1));
                    LOG.info("Reminded person with id {} for {} currently left vacation days", account.getPerson().getId(), vacationDaysLeft);
                }
            });
    }

    /**
     * Remind for remaining vacation days of last year
     * Should be called after turn of the year logic which calculates the new account for the new year
     */
    @Async
    public void remindForRemainingVacationDays() {

        final Year year = Year.now(clock);
        final List<Person> persons = personService.getActivePersons();

        final List<Account> holidaysAccounts = accountService.getHolidaysAccount(year.getValue(), persons);

        if (!holidaysAccounts.isEmpty()) {

            final Map<Person, WorkingTimeCalendar> workingTimesByPersons = workingTimeCalendarService.getWorkingTimesByPersons(persons, year);
            final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeft(holidaysAccounts, workingTimesByPersons, year);

            accountHolidayAccountVacationDaysMap.keySet().stream()
                .filter(Account::doRemainingVacationDaysExpire)
                .forEach(account -> {

                    final HolidayAccountVacationDays holidayAccountVacationDays = accountHolidayAccountVacationDaysMap.get(account);
                    final VacationDaysLeft vacationDaysLeft = holidayAccountVacationDays.vacationDaysDateRange();

                    final BigDecimal remainingVacationDaysLeft = vacationDaysLeft.getRemainingVacationDays()
                        .subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

                    if (remainingVacationDaysLeft.compareTo(ZERO) > 0) {
                        sendReminderForRemainingVacationDaysNotification(account.getPerson(), remainingVacationDaysLeft, account.getExpiryDate().minusDays(1));
                        LOG.info("Reminded person with id {} for {} remaining vacation days in year {}.", account.getPerson().getId(), remainingVacationDaysLeft, year);
                    }
                });
        }
    }

    /**
     * Notify about expired remaining vacation days
     */
    @Async
    public void notifyForExpiredRemainingVacationDays() {
        final Year currentYear = Year.now(clock);
        final LocalDate currentDate = LocalDate.now(clock);

        final List<Person> persons = personService.getActivePersons();
        final List<Account> holidaysAccounts = accountService.getHolidaysAccount(currentYear.getValue(), persons);

        if (!holidaysAccounts.isEmpty()) {

            final Map<Person, WorkingTimeCalendar> workingTimesByPersons = workingTimeCalendarService.getWorkingTimesByPersons(persons, currentYear);
            final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeft(holidaysAccounts, workingTimesByPersons, currentYear);

            accountHolidayAccountVacationDaysMap.keySet().stream()
                .filter(Account::doRemainingVacationDaysExpire)
                .forEach(account -> {

                    final HolidayAccountVacationDays holidayAccountVacationDays = accountHolidayAccountVacationDaysMap.get(account);
                    final VacationDaysLeft vacationDaysLeft = holidayAccountVacationDays.vacationDaysDateRange();

                    final LocalDate expiryDate = account.getExpiryDate();
                    if (account.getExpiryNotificationSentDate() == null && (currentDate.isEqual(expiryDate) || currentDate.isAfter(expiryDate))) {

                        final BigDecimal expiredRemainingVacationDays = vacationDaysLeft.getRemainingVacationDays()
                            .subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
                        if (expiredRemainingVacationDays.compareTo(ZERO) > 0) {
                            final BigDecimal totalLeftVacationDays = vacationDaysService.getTotalLeftVacationDays(account);

                            sendNotificationForExpiredRemainingVacationDays(account.getPerson(), expiredRemainingVacationDays, totalLeftVacationDays, vacationDaysLeft.getRemainingVacationDaysNotExpiring(), account.getExpiryDate());
                            LOG.info("Notified person with id {} for {} expired remaining vacation days in year {}.", account.getPerson().getId(), expiredRemainingVacationDays, currentYear);

                            account.setExpiryNotificationSentDate(currentDate);
                            accountService.save(account);
                        }
                    }
                });
        }
    }

    private void sendReminderForCurrentlyLeftVacationDays(Person person, BigDecimal vacationDaysLeft, Year nextYear) {
        final Map<String, Object> model = new HashMap<>();
        model.put("vacationDaysLeft", vacationDaysLeft);
        model.put("nextYear", nextYear.getValue());

        sendMail(person, "subject.account.remindForCurrentlyLeftVacationDays", "account_cron_currently_left_vacation_days", model);
    }

    private void sendReminderForRemainingVacationDaysNotification(Person person, BigDecimal remainingVacationDays, LocalDate dayBeforeExpiryDate) {
        final Map<String, Object> model = new HashMap<>();
        model.put("remainingVacationDays", remainingVacationDays);
        model.put("dayBeforeExpiryDate", dayBeforeExpiryDate);

        sendMail(person, "subject.account.remindForRemainingVacationDays", "account_cron_remind_remaining_vacation_days", model);
    }

    private void sendNotificationForExpiredRemainingVacationDays(Person person, BigDecimal expiredRemainingVacationDays, BigDecimal totalLeftVacationDays, BigDecimal remainingVacationDaysNotExpiring, LocalDate expiryDate) {
        final Map<String, Object> model = new HashMap<>();
        model.put("expiredRemainingVacationDays", expiredRemainingVacationDays);
        model.put("totalLeftVacationDays", totalLeftVacationDays);
        model.put("remainingVacationDaysNotExpiring", remainingVacationDaysNotExpiring);
        model.put("expiryDate", expiryDate);

        sendMail(person, "subject.account.notifyForExpiredRemainingVacationDays", "account_cron_expired_remaining_vacation_days", model);
    }

    private void sendMail(Person person, String subjectMessageKey, String templateName, Map<String, Object> model) {

        model.put("recipientNiceName", person.getNiceName());
        model.put("personId", person.getId());

        final Mail mailToPerson = Mail.builder()
            .withRecipient(person)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, locale -> model)
            .build();
        mailService.send(mailToPerson);
    }
}
