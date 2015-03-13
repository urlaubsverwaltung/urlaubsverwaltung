package org.synyx.urlaubsverwaltung.core.account;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.account.AccountServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class HolidaysAccountServiceImplTest {

    private AccountService accountService;

    private AccountDAO accountDAO;

    @Before
    public void setUp() {

        accountDAO = Mockito.mock(AccountDAO.class);

        accountService = new AccountServiceImpl(accountDAO);
    }


    @Test
    public void testGetAccount() {

        Person person = new Person();
        Account account = new Account();
        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Account result = accountService.getHolidaysAccount(2012, person);

        Assert.assertNotNull(result);
        Assert.assertEquals(account, result);
    }
}
