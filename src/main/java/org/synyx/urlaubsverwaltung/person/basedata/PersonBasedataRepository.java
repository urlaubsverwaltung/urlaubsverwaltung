package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface PersonBasedataRepository extends CrudRepository<PersonBasedataEntity, Integer> {

    List<PersonBasedataEntity> findAllByPersonIdIn(List<Integer> personId);

    void deleteByPerson(Person person);
}
