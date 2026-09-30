package org.synyx.urlaubsverwaltung.account;

import com.icegreen.greenmail.junit5.GreenMailExtension;
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
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class ExpiredRemainingVacationDaysManagementMailServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(SMTP_IMAP);

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private PersonBasedataService personBasedataService;
    @MockitoBean
    private DepartmentService departmentService;

    @Autowired
    private ExpiredRemainingVacationDaysManagementMailService sut;

    @Test
    void ensureOfficeGetsTheExpiredRemainingVacationDaysWithCsv() throws MessagingException, IOException {

        final Person franka = new Person("franka", "Potente", "Franka", "franka.potente@example.org");
        franka.setId(1L);
        final Person michel = new Person("michel", "Schneider", "Michel", "michel.schneider@example.org");
        michel.setId(2L);

        final Person office = new Person("office", "Office", "Senorita", "office@example.org");
        office.setId(3L);
        office.setPermissions(List.of(USER, OFFICE));
        office.setNotifications(List.of(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL));
        when(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).thenReturn(List.of(office));

        when(personBasedataService.getBasedataByPersonId(List.of(1L, 2L))).thenReturn(Map.of(new PersonId(1L), new PersonBasedata(new PersonId(1L), "42", "")));
        when(departmentService.getDepartmentNamesByMembers(List.of(franka, michel))).thenReturn(Map.of(new PersonId(1L), List.of("Entwicklung", "Marketing")));

        sut.sendExpiredRemainingVacationDaysNotification(List.of(
            new ExpiredRemainingVacationDays(account(michel), ONE, ZERO, new BigDecimal("0.5")),
            new ExpiredRemainingVacationDays(account(franka), new BigDecimal("3"), ONE, new BigDecimal("12"))
        ));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        final MimeMessage msg = greenMail.getReceivedMessagesForDomain(office.getEmail())[0];
        assertThat(msg.getSubject()).isEqualTo("Verfallener Resturlaub");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        final Email email = EmailConverter.mimeMessageToEmail(msg);
        assertThat(email.getPlainText().replaceAll("\\r", "")).isEqualTo("""
            Hallo Senorita Office,

            folgender Resturlaub ist verfallen:

            Franka Potente: 3 Tage verfallen am 01.04.2027, noch 12 Tage Urlaub übrig
            Michel Schneider: 1 Tag verfallen am 01.04.2027, noch 0,5 Tage Urlaub übrig

            Die Aufstellung findest du auch als CSV-Datei im Anhang.
            """);

        final List<AttachmentResource> attachments = email.getAttachments();
        assertThat(attachments).hasSize(1);
        assertThat(attachments.getFirst().getName()).isEqualTo("Verfallener-Resturlaub_%s_de.csv".formatted(LocalDate.now()));
        // line endings of text attachments are CRLF after the transport
        assertThat(attachments.getFirst().readAllData(UTF_8).replaceAll("\\r", "")).isEqualTo("""
            ﻿Personalnummer;Vorname;Nachname;Abteilungen;Verfallsdatum;Verfallene Resturlaubstage;Nicht verfallener Resturlaub;Verbleibende Urlaubstage
            42;Franka;Potente;Entwicklung, Marketing;01.04.2027;3;1;12
            ;Michel;Schneider;;01.04.2027;1;0;0,5
            """);
    }

    private static Account account(Person person) {
        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2027, 4, 1));
        return account;
    }
}
