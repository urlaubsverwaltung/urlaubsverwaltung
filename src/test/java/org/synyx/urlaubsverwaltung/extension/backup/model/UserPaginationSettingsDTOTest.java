package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UserPaginationSettingsDTOTest {

    @Test
    void happyPath() {
        UserPaginationSettingsDTO dto = new UserPaginationSettingsDTO(20);
        UserPaginationSettingsEntity entity = dto.toUserPaginationSettingsEntity(1L);

        assertThat(entity.getPersonId()).isEqualTo(1L);
        assertThat(entity.getDefaultPageSize()).isEqualTo(dto.defaultPageSize());
    }


}
