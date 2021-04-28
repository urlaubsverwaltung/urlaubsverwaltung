package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.LegacyMail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

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

    @BeforeEach
    void setUp() {
        sut = new TurnOfTheYearAccountUpdaterService(personService, accountService, accountInteractionService, mailService, clock);
    }

    @Test
    void ensureUpdatesHolidaysAccountsOfAllActivePersons() {

        Person user1 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        Person user2 = new Person("muster", "Muster", "Marlene", "muster@example.org");
        Person user3 = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Account account1 = createHolidaysAccount(user1, LAST_YEAR);
        Account account2 = createHolidaysAccount(user2, LAST_YEAR);
        Account account3 = createHolidaysAccount(user3, LAST_YEAR);

        when(personService.getActivePersons()).thenReturn(asList(user1, user2, user3));
        when(accountService.getHolidaysAccount(LAST_YEAR, user1)).thenReturn(Optional.of(account1));
        when(accountService.getHolidaysAccount(LAST_YEAR, user2)).thenReturn(Optional.of(account2));
        when(accountService.getHolidaysAccount(LAST_YEAR, user3)).thenReturn(Optional.of(account3));

        Account newAccount = mock(Account.class);
        when(newAccount.getRemainingVacationDays()).thenReturn(BigDecimal.TEN);
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(any(Account.class)))
            .thenReturn(newAccount);

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

        final ArgumentCaptor<LegacyMail> argument = ArgumentCaptor.forClass(LegacyMail.class);
        verify(mailService, times(2)).legacySend(argument.capture());
        final List<LegacyMail> mails = argument.getAllValues();
        assertThat(mails.get(0).getMailNotificationRecipients()).hasValue(NOTIFICATION_OFFICE);
        assertThat(mails.get(0).getSubjectMessageKey()).isEqualTo("subject.account.updatedRemainingDays");
        assertThat(mails.get(0).getTemplateName()).isEqualTo("updated_accounts");
        assertThat(mails.get(1).isSendToTechnicalMail()).isTrue();
        assertThat(mails.get(1).getSubjectMessageKey()).isEqualTo("subject.account.updatedRemainingDays");
        assertThat(mails.get(1).getTemplateName()).isEqualTo("updated_accounts");
    }
}
