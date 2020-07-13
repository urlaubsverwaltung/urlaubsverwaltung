package org.synyx.urlaubsverwaltung.account.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.dao.AccountRepository;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.account.service.AccountServiceImpl}.
 */
class AccountServiceImplTest {

    private AccountService accountService;

    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);

        accountService = new AccountServiceImpl(accountRepository);
    }


    @Test
    void ensureReturnsOptionalWithHolidaysAccountIfExists() {

        Person person = DemoDataCreator.createPerson();
        Account account = DemoDataCreator.createHolidaysAccount(person, 2012);
        when(accountRepository.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, person);

        Assert.assertNotNull("Optional must not be null", optionalHolidaysAccount);
        Assert.assertTrue("Holidays account should exist", optionalHolidaysAccount.isPresent());
        Assert.assertEquals("Wrong holidays account", account, optionalHolidaysAccount.get());
    }


    @Test
    void ensureReturnsAbsentOptionalIfNoHolidaysAccountExists() {

        when(accountRepository.getHolidaysAccountByYearAndPerson(anyInt(), any(Person.class)))
            .thenReturn(null);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, mock(Person.class));

        Assert.assertNotNull("Optional must not be null", optionalHolidaysAccount);
        Assert.assertFalse("Holidays account should not exist", optionalHolidaysAccount.isPresent());
    }
}
