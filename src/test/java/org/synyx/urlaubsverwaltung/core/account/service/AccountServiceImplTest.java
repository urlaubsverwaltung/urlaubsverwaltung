package org.synyx.urlaubsverwaltung.core.account.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.account.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.account.service.AccountServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AccountServiceImplTest {

    private AccountService accountService;

    private AccountDAO accountDAO;

    @Before
    public void setUp() {

        accountDAO = Mockito.mock(AccountDAO.class);

        accountService = new AccountServiceImpl(accountDAO);
    }


    @Test
    public void ensureReturnsOptionalWithHolidaysAccountIfExists() {

        Person person = TestDataCreator.createPerson();
        Account account = TestDataCreator.createHolidaysAccount(person, 2012);
        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, person);

        Assert.assertNotNull("Optional must not be null", optionalHolidaysAccount);
        Assert.assertTrue("Holidays account should exist", optionalHolidaysAccount.isPresent());
        Assert.assertEquals("Wrong holidays account", account, optionalHolidaysAccount.get());
    }


    @Test
    public void ensureReturnsAbsentOptionalIfNoHolidaysAccountExists() {

        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(Mockito.anyInt(), Mockito.any(Person.class)))
            .thenReturn(null);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, Mockito.mock(Person.class));

        Assert.assertNotNull("Optional must not be null", optionalHolidaysAccount);
        Assert.assertFalse("Holidays account should not exist", optionalHolidaysAccount.isPresent());
    }
}
