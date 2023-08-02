package org.synyx.urlaubsverwaltung.user;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;

@Repository
interface UserSettingsRepository extends CrudRepository<UserSettingsEntity, Long> {

    Optional<UserSettingsEntity> findByPersonUsername(String username);

    Optional<UserSettingsEntity> findByPerson(Person person);

    List<UserSettingsEntity> findByPersonIdIn(List<Long> personIds);

    @Modifying
    void deleteByPerson(Person person);
}
