package org.synyx.urlaubsverwaltung.account;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class VacationDaysReminderServiceIT extends TestContainersBase {

    private static final String EMAIL_LINE_BREAK = "\r\n";

    @RegisterExtension
    public final GreenMailExtension greenMail = new GreenMailExtension(SMTP_IMAP);

    @MockBean
    private PersonService personService;
    @MockBean
    private AccountService accountService;
    @MockBean
    private VacationDaysService vacationDaysService;

    @Autowired
    private MailService mailService;

    @Test
    void ensureReminderForLeftVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-10-31T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(42);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account = new Account();
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);

        sut.remindForCurrentlyLeftVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an offenen Urlaubsanspruch");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String content = (String) msg.getContent();
        assertThat(content).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Du hast noch 10 Tage Urlaub für dieses Jahr offen, bitte denke daran, deinen Urlaub zu planen." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/42/overview");
    }

    @Test
    void ensureReminderForRemainingVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-01-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(42);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setExpiryDate(LocalDate.of(2022,4,1));
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);

        sut.remindForRemainingVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an offenen Resturlaub");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String content = (String) msg.getContent();
        assertThat(content).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Du hast noch 10 Tage Resturlaub aus dem Vorjahr, bitte denke daran den Urlaub bis zum 31.03.2022 zu nehmen." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/42/overview");
    }

    @Test
    void ensureReminderForExpiredRemainingVacationDays() throws MessagingException, IOException {

        final Clock clock = Clock.fixed(Instant.parse("2022-04-01T06:00:00Z"), ZoneId.of("UTC"));
        final VacationDaysReminderService sut = new VacationDaysReminderService(personService, accountService, vacationDaysService, mailService, clock);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account account2022 = new Account();
        account2022.setExpiryDate(LocalDate.of(2022,4,1));
        account2022.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account();
        account2023.setExpiryDate(LocalDate.of(2023, 4, 1));
        account2023.setDoRemainingVacationDaysExpireLocally(true);
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023))).thenReturn(vacationDaysLeft);
        when(vacationDaysService.calculateTotalLeftVacationDays(account2022)).thenReturn(TEN);

        sut.notifyForExpiredRemainingVacationDays();

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Verfall des Resturlaubs");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "leider ist dein Resturlaub zum 01.04.2022 in Höhe von 10 Tagen verfallen." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Dein aktueller Urlaubsanspruch:" + EMAIL_LINE_BREAK +
            "    10 Tage" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Mehr Informationen zu deinem Urlaubsanspruch findest du hier: https://localhost:8080/web/person/1/overview");
    }
}
