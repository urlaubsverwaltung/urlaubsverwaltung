package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

@Repository
interface CompanyCalendarRepository extends CrudRepository<CompanyCalendar, Long> {

    Optional<CompanyCalendar> findByPerson(Person person);

    Optional<CompanyCalendar> findBySecretAndPerson(String secret, Person person);

    void deleteByPerson(Person person);

}
