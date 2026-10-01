package org.synyx.urlaubsverwaltung.account;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.TWO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost", "spring.main.allow-bean-definition-overriding=true"})
@Transactional
class TurnOfTheYearAccountUpdaterServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private TurnOfTheYearAccountUpdaterService sut;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private AccountInteractionService accountInteractionService;
    @MockitoBean
    private PersonBasedataService personBasedataService;
    @MockitoBean
    private DepartmentService departmentService;

    @TestConfiguration
    public static class ClockConfig {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2022-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
        }
    }

    @Test
    void ensureToSendSuccessfullyUpdatedAccountsNotification() throws MessagingException, IOException {

        final Person person = new Person("franka", "Potente", "Franka", "franka.potente@example.org");
        final Person person2 = new Person("michel", "Schneider", "Michel", "michel.schneider@example.org");
        person.setId(1L);
        person2.setId(2L);
        when(personService.getActivePersons()).thenReturn(List.of(person, person2));

        final Account account1 = createHolidaysAccount(person, 2021);
        final Account account2 = createHolidaysAccount(person2, 2021);
        when(accountService.getHolidaysAccount(2021, List.of(person, person2))).thenReturn(List.of(account1, account2));

        final Account newAccount1 = createHolidaysAccount(person, 2022);
        newAccount1.setRemainingVacationDays(TEN);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account1)).thenReturn(newAccount1);

        final Account newAccount2 = createHolidaysAccount(person2, 2022);
        newAccount2.setRemainingVacationDays(TWO);
        newAccount2.setDoRemainingVacationDaysExpireLocally(false);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account2)).thenReturn(newAccount2);

        when(personBasedataService.getBasedataByPersonId(List.of(1L, 2L))).thenReturn(Map.of(new PersonId(1L), new PersonBasedata(new PersonId(1L), "42", "")));
        when(departmentService.getDepartmentNamesByMembers(List.of(person, person2))).thenReturn(Map.of(new PersonId(1L), List.of("Entwicklung", "Marketing")));

        final Person office = new Person("office", "Office", "Senorita", "office@example.org");
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_CARRIED_OVER_MANAGEMENT_ALL)).thenReturn(List.of(office));

        sut.updateAccountsForNextPeriod();

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Auswertung Resturlaubstage");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        final Email email = EmailConverter.mimeMessageToEmail(msg);
        assertThat(email.getPlainText().replaceAll("\\r", "")).isEqualTo("""
            Hallo Senorita Office,

            Resturlaubstage zum 01.01.2022 (mitgenommene Resturlaubstage aus dem Vorjahr)

            Franka Potente: 10
            Michel Schneider: 2

            Gesamtzahl an Resturlaubstagen aus dem Vorjahr: 12

            Die Aufstellung findest du auch als CSV-Datei im Anhang.
            """);

        final List<AttachmentResource> attachments = email.getAttachments();
        assertThat(attachments).hasSize(1);
        assertThat(attachments.getFirst().getName()).isEqualTo("Resturlaub_2022_de.csv");
        // line endings of text attachments are CRLF after the transport
        assertThat(attachments.getFirst().readAllData(UTF_8).replaceAll("\\r", "")).isEqualTo("""
            \uFEFFPersonalnummer;Vorname;Nachname;Abteilungen;Resturlaubstage;davon verfallen nicht;Verfallsdatum
            42;Franka;Potente;Entwicklung, Marketing;10;0;01.04.2022
            ;Michel;Schneider;;2;2;
            """);
    }
}
