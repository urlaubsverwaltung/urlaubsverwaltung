package org.synyx.urlaubsverwaltung.core.account;

import com.google.common.base.Optional;

import org.synyx.urlaubsverwaltung.core.person.Person;


/**
 * Implementation of {@link org.synyx.urlaubsverwaltung.core.account.AccountService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;

    public AccountServiceImpl(AccountDAO accountDAO) {

        this.accountDAO = accountDAO;
    }

    @Override
    public Optional<Account> getHolidaysAccount(int year, Person person) {

        return Optional.fromNullable(accountDAO.getHolidaysAccountByYearAndPerson(year, person));
    }


    @Override
    public void save(Account account) {

        accountDAO.save(account);
    }
}
