package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@ExtendWith(MockitoExtension.class)
class DepartmentViewValidatorTest {

    private DepartmentViewValidator sut;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new DepartmentViewValidator();
    }

    @Test
    void ensureSupportsOnlyDepartmentClass() {
        assertThat(sut.supports(null)).isFalse();
        assertThat(sut.supports(DepartmentForm.class)).isTrue();
    }

    @Test
    void ensureNameMustNotBeNull() {
        final DepartmentForm departmentForm = new DepartmentForm();
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeEmpty() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("");
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.mandatory");
    }

    @Test
    void ensureNameMustNotBeTooLong() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.tooManyChars");
    }

    @Test
    void ensureValidNameDoesNotContainDelimiterStart() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName(":::Department");
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.delimiterFound");
    }

    @Test
    void ensureValidNameDoesNotContainDelimiterEnd() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department:::");
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.delimiterFound");
    }

    @Test
    void ensureValidNameDoesNotContainDelimiterMiddle() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Depart:::ment");
        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("name", "error.entry.delimiterFound");
    }

    @Test
    void ensureValidNameDoesContainShortDelimiter() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department::");
        sut.validate(departmentForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidNameHasNoValidationError() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        sut.validate(departmentForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureDescriptionMustNotBeTooLong() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setDescription(
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

        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("description", "error.entry.tooManyChars");
    }

    @Test
    void ensureValidDescriptionHasNoValidationError() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut");
        sut.validate(departmentForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSettingNeitherMembersNorDepartmentHeadsHasNoValidationError() {
        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setMembers(null);
        departmentForm.setDepartmentHeads(null);

        sut.validate(departmentForm, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingAnyMember() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setDepartmentHeads(List.of(person));
        departmentForm.setMembers(null);

        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingThePersonAsMember() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(DEPARTMENT_HEAD));

        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setDepartmentHeads(List.of(person));
        departmentForm.setMembers(List.of(new Person("muster", "Muster", "Marlene", "muster@example.org")));

        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }

    @Test
    void ensureCanNotSetAPersonWithoutDepartmentHeadRoleAsDepartmentHead() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(Role.USER));

        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setDepartmentHeads(List.of(person));

        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadHasNoAccess");
    }

    @Test
    void ensureCanNotSetAPersonWithoutSecondStageAuthorityRoleAsSecondStageAutority() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(Role.USER));

        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setName("Department");
        departmentForm.setSecondStageAuthorities(List.of(person));
        departmentForm.setTwoStageApproval(true);

        sut.validate(departmentForm, errors);
        verify(errors).rejectValue("secondStageAuthorities", "department.members.error.secondStageHasNoAccess");
    }
}
