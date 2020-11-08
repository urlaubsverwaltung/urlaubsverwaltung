package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createHolidaysAccount;


/**
 * Unit test for {@link AccountServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    void ensureReturnsOptionalWithHolidaysAccountIfExists() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Account account = createHolidaysAccount(person, 2012);
        when(accountRepository.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, person);
        assertThat(optionalHolidaysAccount).contains(account);
    }

    @Test
    void ensureReturnsAbsentOptionalIfNoHolidaysAccountExists() {

        when(accountRepository.getHolidaysAccountByYearAndPerson(anyInt(), any(Person.class))).thenReturn(null);

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, mock(Person.class));
        assertThat(optionalHolidaysAccount).isEmpty();
    }
}
