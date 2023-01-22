package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonId;

@Service
class UserPaginationSettingsService {

    private final UserPaginationSettingsRepository repository;
    private final int defaultPageSize;

    protected UserPaginationSettingsService(UserPaginationSettingsRepository repository,
                                  @Value("${spring.data.web.pageable.default-page-size}") int defaultPageSize) {

        this.repository = repository;
        this.defaultPageSize = defaultPageSize;
    }

    UserPaginationSettings getUserPaginationSettings(PersonId personId) {
        return findByPersonId(personId);
    }

    void updatePageableDefaultSize(PersonId personId, int newPageableDefaultSize) {

        final UserPaginationSettings userPaginationSettings = UserPaginationSettings.builder(findByPersonId(personId))
            .defaultPageSize(newPageableDefaultSize)
            .build();

        repository.save(toUserPaginationSettingsEntity(personId, userPaginationSettings));
    }

    private UserPaginationSettings findByPersonId(PersonId personId) {
        return repository.findByPersonId(personId.getValue())
            .map(UserPaginationSettingsService::toUserPaginationSettings)
            .orElseGet(this::defaultUserPaginationSettings);
    }

    private UserPaginationSettings defaultUserPaginationSettings() {
        return new UserPaginationSettings(defaultPageSize);
    }

    private static UserPaginationSettings toUserPaginationSettings(UserPaginationSettingsEntity userPaginationSettingsEntity) {
        return new UserPaginationSettings(userPaginationSettingsEntity.getDefaultPageSize());
    }

    private static UserPaginationSettingsEntity toUserPaginationSettingsEntity(PersonId personId, UserPaginationSettings userPaginationSettings) {
        final UserPaginationSettingsEntity entity = new UserPaginationSettingsEntity();
        entity.setPerson(null);
        entity.setPersonId(personId.getValue());
        entity.setDefaultPageSize(userPaginationSettings.getDefaultPageSize());
        return entity;
    }
}
