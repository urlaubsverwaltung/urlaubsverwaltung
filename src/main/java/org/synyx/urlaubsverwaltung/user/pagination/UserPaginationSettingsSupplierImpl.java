package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

@Service
class UserPaginationSettingsSupplierImpl implements UserPaginationSettingsSupplier {

    private final UserPaginationSettingsService userPaginationSettingsService;

    public UserPaginationSettingsSupplierImpl(UserPaginationSettingsService userPaginationSettingsService) {
        this.userPaginationSettingsService = userPaginationSettingsService;
    }

    @Override
    public UserPaginationSettings getUserPaginationSettings(PersonId personId) {
        return userPaginationSettingsService.getUserPaginationSettings(personId);
    }
}
