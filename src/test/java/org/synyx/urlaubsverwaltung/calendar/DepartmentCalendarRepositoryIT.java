package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class DepartmentCalendarRepositoryIT extends TestContainersBase {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentCalendarRepository sut;

    @Test
    void ensureUniqueConstraintForDepartmentCalendarWithDifferentPersons() {

        final Person savedPersonOne = personService.save(new Person("sam", "smith", "sam", "smith@example.org"));

        final Department department = new Department();
        department.setName("department");
        final Department savedDepartment = departmentService.create(department);

        final DepartmentCalendar firstDepartmentCalendar = new DepartmentCalendar(savedDepartment.getId(), savedPersonOne);
        firstDepartmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstDepartmentCalendar);

        final Person savedPersonTwo = personService.save(new Person("martin", "scissor", "martin", "martin@example.org"));

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar(savedDepartment.getId(), savedPersonTwo);
        departmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(departmentCalendar);

        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    void ensureUniqueConstraintForDepartmentCalendarWithDifferentDepartments() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Department departmentOne = new Department();
        departmentOne.setName("departmentOne");
        final Department savedDepartmentOne = departmentService.create(departmentOne);

        final DepartmentCalendar firstDepartmentCalendar = new DepartmentCalendar(savedDepartmentOne.getId(), savedPerson);
        firstDepartmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstDepartmentCalendar);

        final Department departmentTwo = new Department();
        departmentTwo.setName("departmentTwo");
        final Department savedDepartmentTwo = departmentService.create(departmentTwo);

        final DepartmentCalendar departmentCalendar = new DepartmentCalendar(savedDepartmentTwo.getId(), savedPerson);
        departmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(departmentCalendar);

        assertThat(sut.findAll()).hasSize(2);
    }

    @Test
    void ensureUniqueConstraintForDepartmentCalendar() {

        final Person person = new Person("sam", "smith", "sam", "smith@example.org");
        final Person savedPerson = personService.save(person);

        final Department department = new Department();
        department.setName("department");
        final Department savedDepartment = departmentService.create(department);

        final DepartmentCalendar firstDepartmentCalendar = new DepartmentCalendar(savedDepartment.getId(), savedPerson);
        firstDepartmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstDepartmentCalendar);

        final DepartmentCalendar secondDepartmentCalendar = new DepartmentCalendar(savedDepartment.getId(), savedPerson);
        secondDepartmentCalendar.setCalendarPeriod(Period.ofDays(1));
        final DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> sut.save(secondDepartmentCalendar));
        assertThat(exception.getMessage()).contains("constraint [unique_department_calendar_per_person]");
    }

    @Test
    void ensuresDeletesDepartmentCalendarOnDepartmentDeletion() {

        final Person savedPerson = personService.save(new Person("theodore", "smith", "theodore", "smith@example.org"));

        final Department department = new Department();
        department.setName("department");
        department.setMembers(List.of(savedPerson));
        final Department savedDepartment = departmentService.create(department);

        final Integer departmentId = savedDepartment.getId();
        final DepartmentCalendar firstDepartmentCalendar = new DepartmentCalendar(departmentId, savedPerson);
        firstDepartmentCalendar.setCalendarPeriod(Period.ofDays(1));
        sut.save(firstDepartmentCalendar);

        departmentService.delete(departmentId);

        entityManager.flush();

        final Optional<DepartmentCalendar> departmentCalendar = sut.findByDepartmentIdAndPerson(departmentId, savedPerson);
        assertThat(departmentCalendar).isNotPresent();
    }
}
