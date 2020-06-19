package org.synyx.urlaubsverwaltung.department.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createDepartment;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentValidatorTest {

    private DepartmentValidator sut;

    @Mock
    private Errors errors;

    @Before
    public void setUp() {
        sut = new DepartmentValidator();
    }

    @Test
    public void ensureSupportsOnlyDepartmentClass() {
        assertThat(sut.supports(null)).isFalse();
        assertThat(sut.supports(Application.class)).isFalse();
        assertThat(sut.supports(Department.class)).isTrue();
    }

    @Test
    public void ensureNameMustNotBeNull() {
        final Department department = createDepartment(null);
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    public void ensureNameMustNotBeEmpty() {
        Department department = createDepartment("");
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    public void ensureNameMustNotBeTooLong() {
        Department department = createDepartment("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        sut.validate(department, errors);
        verify(errors).rejectValue("name", "error.entry.tooManyChars");
    }

    @Test
    public void ensureValidNameHasNoValidationError() {
        Department department = createDepartment("Foobar Department");
        sut.validate(department, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureDescriptionMustNotBeTooLong() {
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
    public void ensureValidDescriptionHasNoValidationError() {
        Department department = createDepartment("Foobar Department",
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut");

        sut.validate(department, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureSettingNeitherMembersNorDepartmentHeadsHasNoValidationError() {
        Department department = createDepartment("Admins");
        department.setMembers(null);
        department.setDepartmentHeads(null);

        sut.validate(department, errors);
        verifyZeroInteractions(errors);
    }

    @Test
    public void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingAnyMember() {
        Person person = DemoDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(null);

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    public void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingThePersonAsMember() {
        Person person = DemoDataCreator.createPerson("muster");
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(Collections.singletonList(DemoDataCreator.createPerson("member")));

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    public void ensureCanNotSetAPersonWithoutDepartmentHeadRoleAsDepartmentHead() {
        Person person = DemoDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Department department = createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));

        sut.validate(department, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadHasNoAccess");
    }

    @Test
    public void ensureCanNotSetAPersonWithoutSecondStageAuthorityRoleAsSecondStageAutority() {
        Person person = DemoDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Department department = createDepartment("Admins");
        department.setSecondStageAuthorities(Collections.singletonList(person));
        department.setTwoStageApproval(true);

        sut.validate(department, errors);
        verify(errors).rejectValue("secondStageAuthorities", "department.members.error.secondStageHasNoAccess");
    }
}
