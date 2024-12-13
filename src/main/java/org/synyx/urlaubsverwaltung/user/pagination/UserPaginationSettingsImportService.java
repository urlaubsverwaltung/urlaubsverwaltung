package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.stereotype.Service;

@Service
public class UserPaginationSettingsImportService {

    private final UserPaginationSettingsRepository userPaginationSettingsRepository;

    UserPaginationSettingsImportService(UserPaginationSettingsRepository userPaginationSettingsRepository) {
        this.userPaginationSettingsRepository = userPaginationSettingsRepository;
    }

    public void deleteAll() {
        userPaginationSettingsRepository.deleteAll();
    }

    public void importUserPaginationSettings(UserPaginationSettingsEntity userPaginationSettingsEntity) {
        userPaginationSettingsRepository.save(userPaginationSettingsEntity);
    }
}
