package org.synyx.urlaubsverwaltung.core.account.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.account.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;


/**
 * Implementation of {@link AccountService}.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;

    @Autowired
    public AccountServiceImpl(AccountDAO accountDAO) {

        this.accountDAO = accountDAO;
    }

    @Override
    public Optional<Account> getHolidaysAccount(int year, Person person) {

        return Optional.ofNullable(accountDAO.getHolidaysAccountByYearAndPerson(year, person));
    }


    @Override
    public void save(Account account) {

        accountDAO.save(account);
    }
}
