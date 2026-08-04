package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * {@link ApplicationResponsibility} of one signed in user, asking the {@link DepartmentService} on demand and
 * remembering every answer. Meant to be used for a single request.
 */
class LazyApplicationResponsibility implements ApplicationResponsibility {

    private final DepartmentService departmentService;
    private final Person signedInUser;

    private Boolean departmentHeadOfPerson;
    private Boolean secondStageAuthorityOfPerson;
    private Boolean personIsSecondStageAuthorityOfSignedInUser;

    LazyApplicationResponsibility(DepartmentService departmentService, Person signedInUser) {
        this.departmentService = departmentService;
        this.signedInUser = signedInUser;
    }

    @Override
    public boolean isDepartmentHeadOf(Person person) {
        if (departmentHeadOfPerson == null) {
            departmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, person);
        }
        return departmentHeadOfPerson;
    }

    @Override
    public boolean isSecondStageAuthorityOf(Person person) {
        if (secondStageAuthorityOfPerson == null) {
            secondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, person);
        }
        return secondStageAuthorityOfPerson;
    }

    @Override
    public boolean isSecondStageAuthorityOfSignedInUser(Person person) {
        if (personIsSecondStageAuthorityOfSignedInUser == null) {
            personIsSecondStageAuthorityOfSignedInUser =
                departmentService.isSecondStageAuthorityAllowedToManagePerson(person, signedInUser);
        }
        return personIsSecondStageAuthorityOfSignedInUser;
    }
}
