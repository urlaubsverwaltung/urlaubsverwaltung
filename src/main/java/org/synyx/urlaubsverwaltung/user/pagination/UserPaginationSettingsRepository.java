package org.synyx.urlaubsverwaltung.user.pagination;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface UserPaginationSettingsRepository extends CrudRepository<UserPaginationSettingsEntity, Long> {

    Optional<UserPaginationSettingsEntity> findByPersonId(Long id);
}
