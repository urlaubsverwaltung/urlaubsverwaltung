package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface PersonBasedataRepository extends CrudRepository<PersonBasedataEntity, Long> {

    List<PersonBasedataEntity> findAllByPersonIdIsIn(List<Long> personIds);

    @Modifying
    void deleteByPerson(Person person);
}
