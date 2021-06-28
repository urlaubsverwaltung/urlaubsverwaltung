package org.synyx.urlaubsverwaltung.account.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

class AccountSettingsDtoMapper {

    private AccountSettingsDtoMapper() {
    }

    public static AccountSettingsDto mapToAccountSettingsDto(AccountSettingsEntity accountSettingsEntity) {
        return new ObjectMapper().convertValue(accountSettingsEntity, AccountSettingsDto.class);
    }

    public static AccountSettingsEntity mapToAccountSettingsEntity(AccountSettingsDto accountSettingsDto) {
        return new ObjectMapper().convertValue(accountSettingsDto, AccountSettingsEntity.class);
    }
}
