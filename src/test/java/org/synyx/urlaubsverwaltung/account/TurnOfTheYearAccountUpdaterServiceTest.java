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
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@ExtendWith(MockitoExtension.class)
class TurnOfTheYearAccountUpdaterServiceTest {

    private static final Clock clock = Clock.systemUTC();
    private static final int CURRENT_YEAR = Year.now(clock.getZone()).getValue();
    private static final int LAST_YEAR = CURRENT_YEAR - 1;

    private TurnOfTheYearAccountUpdaterService sut;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private MailService mailService;
    @Mock
    private VacationDaysReminderService vacationDaysReminderService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private RemainingVacationDaysCsvExportService remainingVacationDaysCsvExportService;

    @BeforeEach
    void setUp() {
        sut = new TurnOfTheYearAccountUpdaterService(personService, accountService, accountInteractionService, vacationDaysReminderService, mailService, personBasedataService, departmentService, remainingVacationDaysCsvExportService, clock);
    }

    @Test
    void ensureUpdatesHolidaysAccountsOfAllActivePersons() {

        final Person user1 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person user2 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person user3 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user1.setId(1L);
        user2.setId(2L);
        user3.setId(3L);

        final Account account1 = createHolidaysAccount(user1, LAST_YEAR);
        account1.setId(1L);
        final Account account2 = createHolidaysAccount(user2, LAST_YEAR);
        account2.setId(2L);
        final Account account3 = createHolidaysAccount(user3, LAST_YEAR);
        account3.setId(3L);

        when(personService.getActivePersons()).thenReturn(asList(user1, user2, user3));
        when(accountService.getHolidaysAccount(LAST_YEAR, asList(user1, user2, user3)))
            .thenReturn(asList(account1, account2, account3));

        final Account newAccount = mock(Account.class);
        when(newAccount.getRemainingVacationDays()).thenReturn(BigDecimal.TEN);
        when(newAccount.getPerson()).thenReturn(user1);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(any(Account.class)))
            .thenReturn(newAccount);

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(office));

        sut.updateAccountsForNextPeriod();

        verify(personService).getActivePersons();

        // all last year's accounts are loaded with a single query instead of one query per person
        verify(accountService).getHolidaysAccount(LAST_YEAR, asList(user1, user2, user3));

        verify(accountInteractionService, times(3))
            .autoCreateOrUpdateNextYearsHolidaysAccount(any(Account.class));
        verify(accountInteractionService).autoCreateOrUpdateNextYearsHolidaysAccount(account1);
        verify(accountInteractionService).autoCreateOrUpdateNextYearsHolidaysAccount(account2);
        verify(accountInteractionService).autoCreateOrUpdateNextYearsHolidaysAccount(account3);

        verify(vacationDaysReminderService).remindForRemainingVacationDays();

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getMailAddressRecipients()).hasValue(List.of(office));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.account.updatedRemainingDays");
        assertThat(mail.getTemplateName()).isEqualTo("account_cron_updated_accounts_turn_of_the_year");
        assertThat(mail.getTemplateModel(GERMAN)).containsEntry("totalRemainingVacationDays", BigDecimal.valueOf(30));
    }

    @Test
    void ensureMailHasTheRemainingVacationDaysCsvInTheLocaleOfTheRecipient() {

        final Person person = new Person("franka", "Potente", "Franka", "franka@example.org");
        person.setId(1L);
        when(personService.getActivePersons()).thenReturn(List.of(person));

        final Account lastYear = createHolidaysAccount(person, LAST_YEAR);
        when(accountService.getHolidaysAccount(LAST_YEAR, List.of(person))).thenReturn(List.of(lastYear));

        final Account thisYear = createHolidaysAccount(person, CURRENT_YEAR, new BigDecimal("30"), BigDecimal.TEN, BigDecimal.TWO, "comment");
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(lastYear)).thenReturn(thisYear);

        final PersonBasedata basedata = new PersonBasedata(new PersonId(1L), "42", "");
        when(personBasedataService.getBasedataByPersonId(List.of(1L))).thenReturn(Map.of(new PersonId(1L), basedata));
        when(departmentService.getDepartmentNamesByMembers(List.of(person))).thenReturn(Map.of(new PersonId(1L), List.of("Entwicklung")));

        final ByteArrayResource csv = new ByteArrayResource(new byte[]{1});
        final FilterPeriod currentYear = new FilterPeriod(Year.of(CURRENT_YEAR).atDay(1), Year.of(CURRENT_YEAR).atMonth(12).atEndOfMonth());
        when(remainingVacationDaysCsvExportService.generateCSV(currentYear, GERMAN, List.of(RemainingVacationDaysCsvRow.of(thisYear, basedata, List.of("Entwicklung")))))
            .thenReturn(new CSVFile("Resturlaub_%d_de.csv".formatted(CURRENT_YEAR), csv));

        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(new Person("office", "Office", "Olga", "office@example.org")));

        sut.updateAccountsForNextPeriod();

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        assertThat(argument.getValue().getMailAttachments(GERMAN))
            .hasValue(List.of(new MailAttachment("Resturlaub_%d_de.csv".formatted(CURRENT_YEAR), csv)));

        // basedata and departments are loaded once for all persons
        verify(personBasedataService).getBasedataByPersonId(List.of(1L));
        verify(departmentService).getDepartmentNamesByMembers(List.of(person));
    }

    @Test
    void ensureCsvHasOnlyTheHeaderWithoutUpdatedAccounts() {

        when(personService.getActivePersons()).thenReturn(List.of());
        when(accountService.getHolidaysAccount(LAST_YEAR, List.of())).thenReturn(List.of());
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(new Person("office", "Office", "Olga", "office@example.org")));

        // no rows are passed to the export: the csv consists of the header only
        final FilterPeriod currentYear = new FilterPeriod(Year.of(CURRENT_YEAR).atDay(1), Year.of(CURRENT_YEAR).atMonth(12).atEndOfMonth());
        final ByteArrayResource csv = new ByteArrayResource(new byte[]{});
        when(remainingVacationDaysCsvExportService.generateCSV(currentYear, GERMAN, List.of())).thenReturn(new CSVFile("Resturlaub.csv", csv));

        sut.updateAccountsForNextPeriod();

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        assertThat(argument.getValue().getMailAttachments(GERMAN)).hasValue(List.of(new MailAttachment("Resturlaub.csv", csv)));
    }
}
