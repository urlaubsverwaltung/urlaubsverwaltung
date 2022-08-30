package org.synyx.urlaubsverwaltung.account;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class VacationDaysReminderService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final MailService mailService;
    private final Clock clock;

    @Autowired
    VacationDaysReminderService(PersonService personService, AccountService accountService, VacationDaysService vacationDaysService,
                                MailService mailService, Clock clock) {
        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.mailService = mailService;
        this.clock = clock;
    }

    /**
     * Reminds for vacation days left for <b>current year</b>.
     */
    void remindForCurrentlyLeftVacationDays() {
        final int year = Year.now(clock).getValue();
        final List<Person> persons = personService.getActivePersons();

        for (Person person : persons) {

            accountService.getHolidaysAccount(year, person).ifPresent(account -> {

                final BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account);
                if (vacationDaysLeft.compareTo(ZERO) > 0) {
                    sendReminderForCurrentlyLeftVacationDays(person, vacationDaysLeft, year + 1);
                    LOG.info("Reminded person with id {} for {} currently left vacation days", person.getId(), vacationDaysLeft);
                }
            });
        }
    }

    /**
     * Remind for remaining vacation days of last year
     * Should be called after turn of the year logic which calculates the new account for the new year
     */
    void remindForRemainingVacationDays() {
        final int year = Year.now(clock).getValue();
        final List<Person> persons = personService.getActivePersons();

        for (Person person : persons) {

            accountService.getHolidaysAccount(year, person)
                .filter(Account::isDoRemainingVacationDaysExpire)
                .ifPresent(account -> {
                    final Optional<Account> accountOfNextYear = accountService.getHolidaysAccount(year + 1, person);
                    final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, accountOfNextYear);

                    final BigDecimal remainingVacationDaysLeft = vacationDaysLeft.getRemainingVacationDays()
                        .subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

                    if (remainingVacationDaysLeft.compareTo(ZERO) > 0) {
                        sendReminderForRemainingVacationDaysNotification(person, remainingVacationDaysLeft, account.getExpiryDate().minusDays(1));
                        LOG.info("Reminded person with id {} for {} remaining vacation days in year {}.", person.getId(), remainingVacationDaysLeft, year);
                    }
                });
        }
    }

    /**
     * Notify about expired remaining vacation days
     */
    void notifyForExpiredRemainingVacationDays() {
        final LocalDate now = LocalDate.now(clock);
        final int year = now.getYear();

        final List<Person> persons = personService.getActivePersons();
        for (Person person : persons) {
            accountService.getHolidaysAccount(year, person)
                .filter(Account::isDoRemainingVacationDaysExpire)
                .ifPresent(account -> {
                    final LocalDate expiryDate = account.getExpiryDate();
                    if (account.getExpiryNotificationSentDate() == null && (now.isEqual(expiryDate) || now.isAfter(expiryDate))) {

                        final Optional<Account> accountOfNextYear = accountService.getHolidaysAccount(year + 1, person);
                        final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, accountOfNextYear);

                        final BigDecimal expiredRemainingVacationDays = vacationDaysLeft.getRemainingVacationDays()
                            .subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
                        if (expiredRemainingVacationDays.compareTo(ZERO) > 0) {
                            final BigDecimal totalLeftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(account);

                            sendNotificationForExpiredRemainingVacationDays(person, expiredRemainingVacationDays, totalLeftVacationDays, vacationDaysLeft.getRemainingVacationDaysNotExpiring(), account.getExpiryDate());
                            LOG.info("Notified person with id {} for {} expired remaining vacation days in year {}.", person.getId(), expiredRemainingVacationDays, year);

                            account.setExpiryNotificationSentDate(now);
                            accountService.save(account);
                        }
                    }
                });
        }
    }

    private void sendReminderForCurrentlyLeftVacationDays(Person person, BigDecimal vacationDaysLeft, int nextYear) {
        final Map<String, Object> model = new HashMap<>();
        model.put("vacationDaysLeft", vacationDaysLeft);
        model.put("nextYear", nextYear);

        sendMail(person, "subject.account.remindForCurrentlyLeftVacationDays", "remind_currently_left_vacation_days", model);
    }

    private void sendReminderForRemainingVacationDaysNotification(Person person, BigDecimal remainingVacationDays, LocalDate dayBeforeExpiryDate) {
        final Map<String, Object> model = new HashMap<>();
        model.put("remainingVacationDays", remainingVacationDays);
        model.put("dayBeforeExpiryDate", dayBeforeExpiryDate);

        sendMail(person, "subject.account.remindForRemainingVacationDays", "remind_remaining_vacation_days", model);
    }

    private void sendNotificationForExpiredRemainingVacationDays(Person person, BigDecimal expiredRemainingVacationDays, BigDecimal totalLeftVacationDays, BigDecimal remainingVacationDaysNotExpiring, LocalDate expiryDate) {
        final Map<String, Object> model = new HashMap<>();
        model.put("expiredRemainingVacationDays", expiredRemainingVacationDays);
        model.put("totalLeftVacationDays", totalLeftVacationDays);
        model.put("remainingVacationDaysNotExpiring", remainingVacationDaysNotExpiring);
        model.put("expiryDate", expiryDate);

        sendMail(person, "subject.account.notifyForExpiredRemainingVacationDays", "notify_expired_remaining_vacation_days", model);
    }

    private void sendMail(Person person, String subjectMessageKey, String templateName, Map<String, Object> model) {

        model.put("recipientNiceName", person.getNiceName());
        model.put("personId", person.getId());

        final Mail mailToPerson = Mail.builder()
            .withRecipient(person)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, model)
            .build();
        mailService.send(mailToPerson);
    }
}
