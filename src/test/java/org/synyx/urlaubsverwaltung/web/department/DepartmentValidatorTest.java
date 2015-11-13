package org.synyx.urlaubsverwaltung.web.department;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Collections;

import static junit.framework.TestCase.assertFalse;

import static org.junit.Assert.assertTrue;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public class DepartmentValidatorTest {

    private DepartmentValidator sut;
    private Errors errors = Mockito.mock(Errors.class);

    @Before
    public void setUp() throws Exception {

        sut = new DepartmentValidator();
    }


    @Test
    public void ensureSupportsOnlyDepartmentClass() throws Exception {

        assertFalse(sut.supports(null));
        assertFalse(sut.supports(Application.class));
        assertTrue(sut.supports(Department.class));
    }


    @Test
    public void ensureNameMustNotBeNull() throws Exception {

        sut.validate(TestDataCreator.createDepartment(null), errors);
        Mockito.verify(errors).rejectValue("name", "error.entry.mandatory");
    }


    @Test
    public void ensureNameMustNotBeEmpty() throws Exception {

        Department department = TestDataCreator.createDepartment("");

        sut.validate(department, errors);
        Mockito.verify(errors).rejectValue("name", "error.entry.mandatory");
    }


    @Test
    public void ensureNameMustNotBeTooLong() throws Exception {

        Department department = TestDataCreator.createDepartment(
                "AAAAAAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        sut.validate(department, errors);
        Mockito.verify(errors).rejectValue("name", "error.entry.tooManyChars");
    }


    @Test
    public void ensureValidNameHasNoValidationError() {

        Department department = TestDataCreator.createDepartment("Foobar Department");

        sut.validate(department, errors);
        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureDescriptionMustNotBeTooLong() throws Exception {

        Department department = TestDataCreator.createDepartment("Foobar Department");
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

        Mockito.verify(errors).rejectValue("description", "error.entry.tooManyChars");
    }


    @Test
    public void ensureValidDescriptionHasNoValidationError() {

        Department department = TestDataCreator.createDepartment("Foobar Department",
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut");

        sut.validate(department, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureSettingNeitherMembersNorDepartmentHeadsHasNoValidationError() {

        Department department = TestDataCreator.createDepartment("Admins");
        department.setMembers(null);
        department.setDepartmentHeads(null);

        sut.validate(department, errors);

        Mockito.verifyZeroInteractions(errors);
    }


    @Test
    public void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingAnyMember() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = TestDataCreator.createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(null);

        sut.validate(department, errors);

        Mockito.verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }


    @Test
    public void ensureCanNotSetAPersonAsDepartmentHeadWithoutSettingThePersonAsMember() {

        Person person = TestDataCreator.createPerson("muster");
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));

        Department department = TestDataCreator.createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));
        department.setMembers(Collections.singletonList(TestDataCreator.createPerson("member")));

        sut.validate(department, errors);

        Mockito.verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadNotAssigned");
    }


    @Test
    public void ensureCanNotSetAPersonWithoutDepartmentHeadRoleAsDepartmentHead() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Department department = TestDataCreator.createDepartment("Admins");
        department.setDepartmentHeads(Collections.singletonList(person));

        sut.validate(department, errors);

        Mockito.verify(errors).rejectValue("departmentHeads", "department.members.error.departmentHeadHasNoAccess");
    }
}
