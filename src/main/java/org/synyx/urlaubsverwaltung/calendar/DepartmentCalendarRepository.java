package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;

@Repository
interface DepartmentCalendarRepository extends JpaRepository<DepartmentCalendar, Long> {

    Optional<DepartmentCalendar> findByDepartmentIdAndPerson(Long departmentId, Person person);

    Optional<DepartmentCalendar> findBySecretAndPerson(String secret, Person person);

    List<DepartmentCalendar> findByPersonId(Long personId);

    @Modifying
    void deleteByDepartmentIdAndPerson(Long departmentId, Person person);

    @Modifying
    void deleteByPerson(Person person);
}
