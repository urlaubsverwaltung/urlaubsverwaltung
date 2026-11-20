package org.synyx.urlaubsverwaltung.companyclosure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface CompanyClosureRepository extends JpaRepository<CompanyClosureEntity, Long> {

    List<CompanyClosureEntity> findByLastModifiedBy(Person lastModifiedBy);
}
