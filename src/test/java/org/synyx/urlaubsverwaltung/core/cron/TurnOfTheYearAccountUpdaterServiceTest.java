package org.synyx.urlaubsverwaltung.core.cron;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
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

        personServiceMock = Mockito.mock(PersonService.class);
        accountServiceMock = Mockito.mock(AccountService.class);
        accountInteractionServiceMock = Mockito.mock(AccountInteractionService.class);
        mailServiceMock = Mockito.mock(MailService.class);

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

        Account newAccount = Mockito.mock(Account.class);
        when(newAccount.getRemainingVacationDays()).thenReturn(BigDecimal.TEN);
        when(accountInteractionServiceMock.autoCreateOrUpdateNextYearsHolidaysAccount(
                    Mockito.any(Account.class)))
            .thenReturn(newAccount);

        sut.updateHolidaysAccounts();

        Mockito.verify(personServiceMock).getActivePersons();

        Mockito.verify(accountServiceMock, Mockito.times(3))
            .getHolidaysAccount(Mockito.anyInt(), Mockito.any(Person.class));
        Mockito.verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user1);
        Mockito.verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user2);
        Mockito.verify(accountServiceMock).getHolidaysAccount(LAST_YEAR, user3);

        Mockito.verify(accountInteractionServiceMock, Mockito.times(3))
            .autoCreateOrUpdateNextYearsHolidaysAccount(Mockito.any(Account.class));
        Mockito.verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account1);
        Mockito.verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account2);
        Mockito.verify(accountInteractionServiceMock).autoCreateOrUpdateNextYearsHolidaysAccount(account3);

        Mockito.verify(mailServiceMock).sendSuccessfullyUpdatedAccountsNotification(Mockito.anyListOf(Account.class));
    }
}
