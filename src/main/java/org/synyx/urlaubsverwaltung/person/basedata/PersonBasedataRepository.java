package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface PersonBasedataRepository extends CrudRepository<PersonBasedataEntity, Integer> {

    List<PersonBasedataEntity> findAllByPersonIdIsIn(List<Integer> personIds);

    @Modifying
    void deleteByPerson(Person person);
}
