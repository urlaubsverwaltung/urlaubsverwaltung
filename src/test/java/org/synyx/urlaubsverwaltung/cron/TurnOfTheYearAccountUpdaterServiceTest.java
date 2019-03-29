package org.synyx.urlaubsverwaltung.cron;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TurnOfTheYearAccountUpdaterServiceTest {

    private static final int NEW_YEAR = DateMidnight.now().getYear();
    private static final int LAST_YEAR = NEW_YEAR - 1;

    private PersonService personServiceMock;
    private AccountService accountServiceMock;
    private AccountInteractionService accountInteractionServiceMock;
    private MailService mailServiceMock;

    private TurnOfTheYearAccountUpdaterService sut;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
        accountServiceMock = mock(AccountService.class);
        accountInteractionServiceMock = mock(AccountInteractionService.class);
        mailServiceMock = mock(MailService.class);

        sut = new TurnOfTheYearAccountUpdaterService(personServiceMock, accountServiceMock,
                accountInteractionServiceMock, mailServiceMock);
    }


    @Test
    public void ensureUpdatesHolidaysAccountsOfAllActivePersons() {

        Person user1 = TestDataCreator.createPerson("rick");
        Person user2 = TestDataCreator.createPerson("carl");
        Person user3 = TestDataCreator.createPerson("shane");

        Account account1 = TestDataCreator.createHolidaysAccount(user1, LAST_YEAR);
        Account account2 = TestDataCreator.createHolidaysAccount(user2, LAST_YEAR);
        Account account3 = TestDataCreator.createHolidaysAccount(user3, LAST_YEAR);

        when(personServiceMock.getActivePersons()).thenReturn(Arrays.asList(user1, user2, user3));
        when(accountServiceMock.getHolidaysAccount(LAST_YEAR, user1)).thenReturn(Optional.of(account1));
        when(accountServiceMock.getHolidaysAccount(LAST_YEAR, user2)).thenReturn(Optional.of(account2));
        when(accountServiceMock.getHolidaysAccount(LAST_YEAR, user3)).thenReturn(Optional.of(account3));

        Account newAccount = mock(Account.class);
        when(newAccount.getRemainingVacationDays()).thenReturn(BigDecimal.TEN);
        when(accountInteractionServiceMock.autoCreateOrUpdateNextYearsHolidaysAccount(
                    any(Account.class)))
            .thenReturn(newAccount);

        sut.updateHolidaysAccounts();

        verify(personServiceMock).getActivePersons();

        verify(accountServiceMock, times(3))
            .getHolidaysAccount(anyInt(), any(Person.class));
        verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user1);
        verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user2);
        verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user3);

        verify(accountInteractionServiceMock, times(3))
            .autoCreateOrUpdateNextYearsHolidaysAccount(any(Account.class));
        verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account1);
        verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account2);
        verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account3);

        verify(mailServiceMock).sendSuccessfullyUpdatedAccountsNotification(anyList());
    }
}
