package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsEntity;

public record UserPaginationSettingsDTO(int defaultPageSize) {

    public UserPaginationSettingsEntity toUserPaginationSettingsEntity(Long personId) {
        final UserPaginationSettingsEntity entity = new UserPaginationSettingsEntity();
        entity.setPersonId(personId);
        entity.setDefaultPageSize(this.defaultPageSize);
        return entity;
    }
}
