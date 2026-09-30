package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.csv.CSVFile;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailAttachment;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ExpiredRemainingVacationDaysManagementMailServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 1);

    private ExpiredRemainingVacationDaysManagementMailService sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ExpiredRemainingVacationDaysCsvExportService csvExportService;
    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(Instant.parse("2027-04-01T06:00:00Z"), ZoneId.of("UTC"));
        sut = new ExpiredRemainingVacationDaysManagementMailService(personService, personBasedataService, departmentService, csvExportService, mailService, clock);
    }

    @Test
    void ensureOfficeGetsTheExpiredRemainingVacationDaysSortedByNameWithCsv() {

        final Person franka = person(1L, "Franka", "Potente");
        final Person michel = person(2L, "Michel", "Schneider");
        final ExpiredRemainingVacationDays michelExpired = new ExpiredRemainingVacationDays(account(michel), ONE, ZERO, new BigDecimal("0.5"));
        final ExpiredRemainingVacationDays frankaExpired = new ExpiredRemainingVacationDays(account(franka), new BigDecimal("3"), ONE, new BigDecimal("12"));

        final Person office = office(3L);
        // still has the notification stored, but is no office anymore
        final Person formerOffice = person(4L, "Former", "Office");
        formerOffice.setPermissions(List.of(USER));
        when(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).thenReturn(List.of(office, formerOffice));

        final PersonBasedata frankaBasedata = new PersonBasedata(new PersonId(1L), "42", "");
        when(personBasedataService.getBasedataByPersonId(List.of(1L, 2L))).thenReturn(Map.of(new PersonId(1L), frankaBasedata));
        when(departmentService.getDepartmentNamesByMembers(List.of(franka, michel))).thenReturn(Map.of(new PersonId(1L), List.of("Entwicklung")));

        final List<ExpiredRemainingVacationDaysCsvRow> rows = List.of(
            ExpiredRemainingVacationDaysCsvRow.of(frankaExpired, frankaBasedata, List.of("Entwicklung")),
            ExpiredRemainingVacationDaysCsvRow.of(michelExpired, null, null)
        );
        final ByteArrayResource csv = new ByteArrayResource(new byte[]{1});
        when(csvExportService.generateCSV(new FilterPeriod(TODAY, TODAY), GERMAN, rows))
            .thenReturn(new CSVFile("Verfallener-Resturlaub_2027-04-01_de.csv", csv));

        sut.sendExpiredRemainingVacationDaysNotification(List.of(michelExpired, frankaExpired));

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).contains(List.of(office));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.account.expiredRemainingVacationDays.management");
        assertThat(mail.getTemplateName()).isEqualTo("account_cron_expired_remaining_vacation_days_management");
        assertThat(mail.getTemplateModel(GERMAN)).containsExactly(entry("expiredRemainingVacationDays", List.of(frankaExpired, michelExpired)));
        assertThat(mail.getMailAttachments(GERMAN)).hasValue(List.of(new MailAttachment("Verfallener-Resturlaub_2027-04-01_de.csv", csv)));

        // basedata and departments are loaded once for all persons
        verify(personBasedataService).getBasedataByPersonId(List.of(1L, 2L));
        verify(departmentService).getDepartmentNamesByMembers(List.of(franka, michel));
    }

    @Test
    void ensureNoMailWithoutOfficeHavingTheNotification() {

        when(personService.getActivePersonsWithNotificationType(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL)).thenReturn(List.of());

        final Person franka = person(1L, "Franka", "Potente");
        sut.sendExpiredRemainingVacationDaysNotification(List.of(new ExpiredRemainingVacationDays(account(franka), ONE, ZERO, ONE)));

        verifyNoInteractions(mailService, csvExportService, personBasedataService, departmentService);
    }

    private static Person person(long id, String firstName, String lastName) {
        final Person person = new Person(firstName.toLowerCase(), lastName, firstName, firstName.toLowerCase() + "@example.org");
        person.setId(id);
        return person;
    }

    private static Person office(long id) {
        final Person office = person(id, "Olga", "Office");
        office.setPermissions(List.of(USER, OFFICE));
        return office;
    }

    private static Account account(Person person) {
        final Account account = new Account();
        account.setPerson(person);
        account.setExpiryDateLocally(LocalDate.of(2027, 4, 1));
        return account;
    }
}
