package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class DepartmentRepositoryIT extends SingleTenantTestContainersBase {

    @Autowired
    private DepartmentRepository sut;

    @Autowired
    private PersonService personService;

    @Test
    void ensuresThatManagedMembersOfPersonWithRoleDHAndSSAareFound() {

        final Person savedPerson = personService.create("muster", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        final Person savedMemberA = personService.create("memberA", "memberA", "memberA", "memberA@example.org", List.of(), List.of(USER));

        final DepartmentMemberEmbeddable departmentMemberEmbeddableA = new DepartmentMemberEmbeddable();
        departmentMemberEmbeddableA.setAccessionDate(Instant.now());
        departmentMemberEmbeddableA.setPerson(savedMemberA);

        final DepartmentEntity departmentA = new DepartmentEntity();
        departmentA.setName("departmentA");
        departmentA.setCreatedAt(LocalDate.of(2022, 10, 2));
        departmentA.setMembers(List.of(departmentMemberEmbeddableA));
        departmentA.setDepartmentHeads(List.of(savedPerson));
        final DepartmentEntity savedDepartmentA = sut.save(departmentA);

        final Person savedMemberB = personService.create("memberB", "memberB", "memberB", "memberB@example.org", List.of(), List.of(USER));

        final DepartmentMemberEmbeddable departmentMemberEmbeddableB = new DepartmentMemberEmbeddable();
        departmentMemberEmbeddableB.setAccessionDate(Instant.now());
        departmentMemberEmbeddableB.setPerson(savedMemberB);

        final DepartmentEntity departmentB = new DepartmentEntity();
        departmentB.setName("departmentB");
        departmentB.setCreatedAt(LocalDate.of(2022, 10, 2));
        departmentB.setMembers(List.of(departmentMemberEmbeddableB));
        departmentB.setSecondStageAuthorities(List.of(savedPerson));
        final DepartmentEntity savedDepartmentB = sut.save(departmentB);

        final List<DepartmentEntity> departments = sut.findByDepartmentHeadsOrSecondStageAuthorities(savedPerson, savedPerson);
        assertThat(departments).containsOnly(savedDepartmentA, savedDepartmentB);
    }
}
