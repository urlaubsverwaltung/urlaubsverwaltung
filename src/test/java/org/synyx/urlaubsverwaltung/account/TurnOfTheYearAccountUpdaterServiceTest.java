package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @BeforeEach
    void setUp() {
        sut = new TurnOfTheYearAccountUpdaterService(personService, accountService, accountInteractionService, vacationDaysReminderService, mailService, clock);
    }

    @Test
    void ensureUpdatesHolidaysAccountsOfAllActivePersons() {

        final Person user1 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person user2 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person user3 = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Account account1 = createHolidaysAccount(user1, LAST_YEAR);
        account1.setId(1L);
        final Account account2 = createHolidaysAccount(user2, LAST_YEAR);
        account2.setId(2L);
        final Account account3 = createHolidaysAccount(user3, LAST_YEAR);
        account3.setId(3L);

        when(personService.getActivePersons()).thenReturn(asList(user1, user2, user3));
        when(accountService.getHolidaysAccount(LAST_YEAR, user1)).thenReturn(Optional.of(account1));
        when(accountService.getHolidaysAccount(LAST_YEAR, user2)).thenReturn(Optional.of(account2));
        when(accountService.getHolidaysAccount(LAST_YEAR, user3)).thenReturn(Optional.of(account3));

        final Account newAccount = mock(Account.class);
        when(newAccount.getRemainingVacationDays()).thenReturn(BigDecimal.TEN);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(any(Account.class)))
            .thenReturn(newAccount);

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(List.of(office));

        sut.updateAccountsForNextPeriod();

        verify(personService).getActivePersons();

        verify(accountService, times(3))
            .getHolidaysAccount(anyInt(), any(Person.class));
        verify(accountService).getHolidaysAccount(LAST_YEAR, user1);
        verify(accountService).getHolidaysAccount(LAST_YEAR, user2);
        verify(accountService).getHolidaysAccount(LAST_YEAR, user3);

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
}
