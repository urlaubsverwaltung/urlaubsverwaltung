package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;


/**
 * Implementation of {@link AccountService}.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {

        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getHolidaysAccount(int year, Person person) {

        return Optional.ofNullable(accountRepository.getHolidaysAccountByYearAndPerson(year, person));
    }


    @Override
    public Account save(Account account) {

        return accountRepository.save(account);
    }
}
