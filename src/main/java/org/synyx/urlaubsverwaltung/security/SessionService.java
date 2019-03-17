package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.Optional;


@Service
public class SessionService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    public SessionService(PersonService personService, DepartmentService departmentService) {

        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * This method allows to get the signed in user.
     *
     * @return  user that is signed in
     */
    public Person getSignedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found in context.");
        }

        String user = authentication.getName();

        Optional<Person> person = personService.getPersonByLogin(user);

        if (!person.isPresent()) {
            throw new IllegalStateException("Can not get the person for the signed in user with username = " + user);
        }

        return person.get();
    }


    /**
     * Check if the given signed in user is allowed to access the data of the given person.
     *
     * @param  signedInUser  to check the permissions
     * @param  person  which data should be accessed
     *
     * @return  {@code true} if the given user may access the data of the given person, else {@code false}
     */
    public boolean isSignedInUserAllowedToAccessPersonData(Person signedInUser, Person person) {

        boolean isOwnData = person.getId().equals(signedInUser.getId());
        boolean isBossOrOffice = signedInUser.hasRole(Role.OFFICE) || signedInUser.hasRole(Role.BOSS);
        boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadOfPerson(signedInUser, person);
        boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityOfPerson(signedInUser, person);
        boolean isPrivilegedUser = isBossOrOffice || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson;

        // Note:
        // signedInUser has role DEPARTMENT_HEAD
        // person has role SECOND_STAGE_AUTHORITY
        // signedInUser and person are in the same department
        // signedInUser is not allowed to access persons data cause of lower level role
        // (DEPARTMENT_HEAD < SECOND_STAGE_AUTHORITY)
        boolean isDepartmentHeadOfSecondStageAuthority =
                person.hasRole(Role.SECOND_STAGE_AUTHORITY) && signedInUser.hasRole(Role.DEPARTMENT_HEAD);

        return isOwnData || (isPrivilegedUser && !isDepartmentHeadOfSecondStageAuthority);
    }
}
