package org.synyx.urlaubsverwaltung.companyclosure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface CompanyClosureVersionRepository extends JpaRepository<CompanyClosureVersionEntity, Long> {

    List<CompanyClosureVersionEntity> findByLastModifiedBy(Person lastModifiedBy);
}
