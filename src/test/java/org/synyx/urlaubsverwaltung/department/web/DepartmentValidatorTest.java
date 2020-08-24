package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;

@ExtendWith(MockitoExtension.class)
class DepartmentValidatorTest {

    private DepartmentValidator sut;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new DepartmentValidator();
    }

    @Test
    void ensureSupportsOnlyDepartmentClass() {
        assertThat(sut.supports(null)).isFalse();
        assertThat(sut.supports(Application.class)).isFalse();
        assertThat(sut.supports(Department.class)).isTrue();
    }

    @Test
    void ensureNameMustNotBeNull() {
        final Department department = createDepartment(null);
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeEmpty() {
        Department department = createDepartment("");
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeTooLong() {
        Department department = createDepartment("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.tooManyChars");
    }

    @Test
    void ensureValidNameHasNoValidationError() {
        Department department = createDepartment("Foobar Department");
        sut.validate(department, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureDescriptionMustNotBeTooLong() {
        Department department = createDepartment("Foobar Department");
        department.setDescription(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut "
                + "labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo"
                + " dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor"
                + " sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod"
                + " tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et"
                + " accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus"
                + " est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed"
                + " diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua."
                + " At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea"
                + " takimata sanctus est Lorem ipsum dolor sit amet igir.");

        sut.validate(department, errors);
        verify(errors).rejectValue("description", "error.entry.tooManyChars");
    }

    @Test
    void ensureValidDescriptionHasNoValidationError() {
        Department department = createDepartment("Foobar Department",
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut");

        sut.validate(department, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSettingNeitherMembersNorDepartmentHeadsHasNoValidationError() {
        Department department = createDepartment("Admins");
        department.setMembers(null);
        department.setDepartmentHeads(null);

        sut.validate(department, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingAnyMember() {
        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(null);

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingThePersonAsMember() {
        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(Collections.singletonList(TestDataCreator.createPerson()));

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    void ensureCanNotSetAPersonWithoutDepartmentHeadRoleAsDepartmentHead() {
        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadHasNoAccess");
    }

    @Test
    void ensureCanNotSetAPersonWithoutSecondStageAuthorityRoleAsSecondStageAutority() {
        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Department department = createDepartment("Admins");
        department.setSecondStageAuthorities(Collections.singletonList(person));
        department.setTwoStageApproval(true);

        sut.validate(department, errors);
        verify(errors).rejectValue("secondStageAuthorities", "department.members.error.secondStageHasNoAccess");
    }
}
