package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

@Repository
interface CompanyCalendarRepository extends CrudRepository<CompanyCalendar, Long> {

    CompanyCalendar findBySecretAndPerson(String secret, Person person);
}
