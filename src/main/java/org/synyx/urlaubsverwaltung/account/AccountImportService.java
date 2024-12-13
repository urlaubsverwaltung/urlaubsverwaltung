package org.synyx.urlaubsverwaltung.account;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountImportService {

    private final AccountRepository accountRepository;

    public AccountImportService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void deleteAll() {
        accountRepository.deleteAll();
    }

    public void importAccounts(List<AccountEntity> accounts) {
        accountRepository.saveAll(accounts);
    }
}
