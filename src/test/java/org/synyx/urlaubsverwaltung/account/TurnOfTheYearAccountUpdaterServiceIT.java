package org.synyx.urlaubsverwaltung.account;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.TWO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

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
        when(personService.getActivePersons()).thenReturn(List.of(person, person2));

        final Account account1 = createHolidaysAccount(person, 2021);
        when(accountService.getHolidaysAccount(2021, person)).thenReturn(Optional.of(account1));

        final Account account2 = createHolidaysAccount(person2, 2021);
        when(accountService.getHolidaysAccount(2021, person2)).thenReturn(Optional.of(account2));

        final Account newAccount1 = createHolidaysAccount(person, 2022);
        newAccount1.setRemainingVacationDays(TEN);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account1)).thenReturn(newAccount1);

        final Account newAccount2 = createHolidaysAccount(person2, 2022);
        newAccount2.setRemainingVacationDays(TWO);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account2)).thenReturn(newAccount2);

        final Person office = new Person("office", "Office", "Senorita", "office@example.org");
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(office));

        sut.updateAccountsForNextPeriod();

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Auswertung Resturlaubstage");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Senorita Office,

            Resturlaubstage zum 01.01.2022 (mitgenommene Resturlaubstage aus dem Vorjahr)

            Franka Potente: 10
            Michel Schneider: 2

            Gesamtzahl an Resturlaubstagen aus dem Vorjahr: 12""");
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }
}
