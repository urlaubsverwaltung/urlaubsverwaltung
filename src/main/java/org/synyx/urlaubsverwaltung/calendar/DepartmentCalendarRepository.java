package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;

@Repository
interface DepartmentCalendarRepository extends CrudRepository<DepartmentCalendar, Long> {

    DepartmentCalendar findByDepartmentAndPerson(Department department, Person person);

    DepartmentCalendar findBySecretAndPerson(String secret, Person person);

    @Modifying
    void deleteByDepartmentAndPerson(Department department, Person person);
}
