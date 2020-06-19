package org.synyx.urlaubsverwaltung.account.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createHolidaysAccount;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class TurnOfTheYearAccountUpdaterServiceTest {

    private static final int NEW_YEAR = ZonedDateTime.now(UTC).getYear();
    private static final int LAST_YEAR = NEW_YEAR - 1;

    private TurnOfTheYearAccountUpdaterService sut;

    @Mock
    private PersonService personService;
    @Mock
    private AccountService accountService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private MailService mailService;

    @Before
    public void setUp() {
        sut = new TurnOfTheYearAccountUpdaterService(personService, accountService, accountInteractionService, mailService);
    }

    @Test
    public void ensureUpdatesHolidaysAccountsOfAllActivePersons() {

        Person user1 = createPerson("rick");
        Person user2 = createPerson("carl");
        Person user3 = createPerson("shane");

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

        verify(mailService).sendMailTo(eq(NOTIFICATION_OFFICE), eq("subject.account.updatedRemainingDays"), eq("updated_accounts"), any());
        verify(mailService).sendTechnicalMail(eq("subject.account.updatedRemainingDays"), eq("updated_accounts"), any());
    }
}
