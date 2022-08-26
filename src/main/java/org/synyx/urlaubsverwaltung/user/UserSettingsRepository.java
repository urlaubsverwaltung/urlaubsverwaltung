package org.synyx.urlaubsverwaltung.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

@Repository
interface UserSettingsRepository extends CrudRepository<UserSettingsEntity, Integer> {
    Optional<UserSettingsEntity> findByPersonUsername(String username);
    void deleteByPerson(Person person);
}
