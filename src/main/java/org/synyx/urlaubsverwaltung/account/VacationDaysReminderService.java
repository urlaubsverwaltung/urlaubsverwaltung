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
    public VacationDaysReminderService(PersonService personService, AccountService accountService, VacationDaysService vacationDaysService, MailService mailService, Clock clock) {

        this.personService = personService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.mailService = mailService;
        this.clock = clock;
    }

    void remindForVacationDaysLeft() {
        final int year = Year.now(clock).getValue();
        final List<Person> persons = personService.getActivePersons();

        for (Person person : persons) {

            accountService.getHolidaysAccount(year, person).ifPresent(account -> {

                final BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account);

                if (vacationDaysLeft.compareTo(ZERO) > 0) {
                    LOG.info("Remind person with id {} for {} vacation days left in year {}.",
                        person.getId(), vacationDaysLeft, year);
                    sendReminderForVacationDaysLeftNotification(person, vacationDaysLeft, year + 1);
                }
            });
        }
    }

    void remindForRemainingVacationDays() {
        //TODO: how to ensure that this is called after turn of the year update?

        final int year = Year.now(clock).getValue();
        final List<Person> persons = personService.getActivePersons();

        for (Person person : persons) {

            accountService.getHolidaysAccount(year, person).ifPresent(account -> {

                final Optional<Account> accountOfNextYear = accountService.getHolidaysAccount(year + 1, person);
                final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, accountOfNextYear);

                final BigDecimal remainingVacationDaysLeft = vacationDaysLeft.getRemainingVacationDays().subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

                if (remainingVacationDaysLeft.compareTo(ZERO) > 0) {
                    LOG.info("Remind person with id {} for {} remaining vacation days in year {}.",
                        person.getId(), remainingVacationDaysLeft, year);
                    sendReminderForRemainingVacationDaysNotification(person, remainingVacationDaysLeft, year);
                }
            });
        }
    }

    void remindForExpiredRemainingVacationDays() {
        final int year = Year.now(clock).getValue();
        final List<Person> persons = personService.getActivePersons();

        for (Person person : persons) {

            accountService.getHolidaysAccount(year, person).ifPresent(account -> {

                final Optional<Account> accountOfNextYear = accountService.getHolidaysAccount(year + 1, person);
                final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, accountOfNextYear);

                final BigDecimal lostRemainingVacationDays = vacationDaysLeft.getRemainingVacationDays().subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

                if (lostRemainingVacationDays.compareTo(ZERO) > 0) {

                    LOG.info("Remind person with id {} for {} lost remaining vacation days in year {}.",
                        person.getId(), lostRemainingVacationDays, year);
                    sendReminderForExpiredRemainingVacationDaysNotification(person, lostRemainingVacationDays, year);
                }
            });
        }
    }

    private void sendReminderForVacationDaysLeftNotification(Person person, BigDecimal vacationDaysLeft, int nextYear) {
        Map<String, Object> model = new HashMap<>();
        model.put("recipientNiceName", person.getNiceName());
        model.put("vacationDaysLeft", vacationDaysLeft);
        model.put("nextYear", nextYear);

        final String subjectMessageKey = "subject.account.remindForVacationDaysLeft";
        final String templateName = "remind_vacation_days_left.ftl";

        final Mail mailToPerson = Mail.builder()
            .withRecipient(person)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, model)
            .build();
        mailService.send(mailToPerson);
    }

    private void sendReminderForRemainingVacationDaysNotification(Person person, BigDecimal remainingVacationDays, int year) {
        Map<String, Object> model = new HashMap<>();
        model.put("recipientNiceName", person.getNiceName());
        model.put("remainingVacationDays", remainingVacationDays);
        model.put("year", year);

        final String subjectMessageKey = "subject.account.remindForRemainingVacationDays";
        final String templateName = "remind_remaining_vacation_days.ftl";

        final Mail mailToPerson = Mail.builder()
            .withRecipient(person)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, model)
            .build();
        mailService.send(mailToPerson);
    }

    private void sendReminderForExpiredRemainingVacationDaysNotification(Person person, BigDecimal expiredRemainingVacationDays, int year) {
        Map<String, Object> model = new HashMap<>();
        model.put("recipientNiceName", person.getNiceName());
        model.put("expiredRemainingVacationDays", expiredRemainingVacationDays);
        model.put("year", year);

        final String subjectMessageKey = "subject.account.remindForExpiredRemainingVacationDays";
        final String templateName = "reminder_expired_remaining_vacation_days.ftl";

        final Mail mailToPerson = Mail.builder()
            .withRecipient(person)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, model)
            .build();
        mailService.send(mailToPerson);
    }
}
