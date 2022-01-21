package org.synyx.urlaubsverwaltung.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserSettingsRepository extends CrudRepository<UserSettingsEntity, Integer> {

}
