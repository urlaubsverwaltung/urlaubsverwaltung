package org.synyx.urlaubsverwaltung.account.settings;

import org.springframework.stereotype.Service;

@Service
public class AccountSettingsService {

    private final AccountSettingsRepository accountSettingsRepository;

    public AccountSettingsService(AccountSettingsRepository accountSettingsRepository) {
        this.accountSettingsRepository = accountSettingsRepository;
    }

    public AccountSettingsDto getSettingsDto() {
        return AccountSettingsDtoMapper.mapToAccountSettingsDto(accountSettingsRepository.findFirstBy());
    }

    public void save(AccountSettingsDto accountSettingsDto) {
        accountSettingsRepository.save(AccountSettingsDtoMapper.mapToAccountSettingsEntity(accountSettingsDto));
    }
}
