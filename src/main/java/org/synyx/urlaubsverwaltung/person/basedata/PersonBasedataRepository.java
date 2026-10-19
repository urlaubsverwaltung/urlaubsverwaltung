package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface PersonBasedataRepository extends CrudRepository<PersonBasedataEntity, Long> {

    List<PersonBasedataEntity> findAllByPersonIdIsIn(List<Long> personIds);
}
