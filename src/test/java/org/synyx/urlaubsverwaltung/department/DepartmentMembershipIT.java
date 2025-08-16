package org.synyx.urlaubsverwaltung.department;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DepartmentMembershipIT extends SingleTenantTestContainersBase {

    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentMembershipRepository departmentMembershipRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ensureMembershipEntriesAreDeletedWhenPersonIsDeleted() {

        final Person person = personService.create("username", "firstname", "lastname", "email@example.com", List.of(), List.of(Role.USER));

        final Department department = new Department();
        department.setName("department");
        department.setMembers(List.of(person));

        departmentService.create(department);
        assertThat(departmentMembershipRepository.findAll()).hasSize(1);

        personService.delete(person, person);
        entityManager.flush();

        assertThat(departmentMembershipRepository.findAll()).isEmpty();
    }
}
