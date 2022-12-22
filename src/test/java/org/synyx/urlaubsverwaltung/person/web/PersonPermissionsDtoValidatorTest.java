package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.BOSS;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.OFFICE;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.USER;


@ExtendWith(MockitoExtension.class)
class PersonPermissionsDtoValidatorTest {

    private PersonPermissionsDtoValidator sut;

    @Mock
    private Errors errors;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new PersonPermissionsDtoValidator(personService);
    }

    @Test
    void ensureSupportsOnlyPersonClass() {

        boolean returnValue;

        returnValue = sut.supports(null);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(Application.class);
        assertThat(returnValue).isFalse();

        returnValue = sut.supports(PersonPermissionsDto.class);
        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureAtLeastOneRoleMustBeSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(new ArrayList<>());

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    void ensureAtLeastOneRoleMustBeSelectedButNull() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(null);

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory");
    }

    @Test
    void ensureIfSelectedInactiveAsRoleNoOtherRoleCanBeSelected() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(INACTIVE, USER));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.inactive");
    }

    @Test
    void ensureSelectingOnlyInactiveRoleIsValid() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(singletonList(INACTIVE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureUserRoleMustBeSelectedIfUserShouldNotBeDeactivated() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(singletonList(OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.user");
    }

    @Test
    void ensureDepartmentHeadRoleAndOfficeRoleCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, OFFICE, DEPARTMENT_HEAD));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSecondStageRoleAndOfficeRoleCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, OFFICE, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureDepartmentHeadRoleAndBossRoleCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, BOSS, DEPARTMENT_HEAD));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSecondStageRoleAndBossRoleCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, BOSS, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidBossRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, BOSS));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidOfficeRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureValidBossAndOfficeRoleSelectionHasNoValidationError() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, BOSS, OFFICE));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureSecondStageRoleAndDepartmentHeadRolesCanBeSelectedBoth() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        sut.validatePermissions(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureAtLeastOnePersonWithTheRoleOfficeIsMe() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setPermissions(List.of(USER, OFFICE));

        sut.validateAtLeastOnePersonWithOffice(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureAtLeastOneOtherPersonWithTheRoleOffice() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setId(1L);
        personPermissionsDto.setPermissions(List.of(USER));

        when(personService.numberOfPersonsWithOfficeRoleExcludingPerson(1)).thenReturn(1);

        sut.validateAtLeastOnePersonWithOffice(personPermissionsDto, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void ensureErrorIfNoPersonWithTheRoleOffice() {

        final PersonPermissionsDto personPermissionsDto = new PersonPermissionsDto();
        personPermissionsDto.setId(1L);
        personPermissionsDto.setPermissions(List.of(USER));

        when(personService.numberOfPersonsWithOfficeRoleExcludingPerson(1)).thenReturn(0);

        sut.validateAtLeastOnePersonWithOffice(personPermissionsDto, errors);
        verify(errors).rejectValue("permissions", "person.form.permissions.error.mandatory.office");
    }
}
