package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Optional;

@Component
public class UserApiMethodSecurity {

    private PersonService personService;
    private DepartmentService departmentService;

    @Autowired
    public UserApiMethodSecurity(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    public boolean isSamePersonOrInDepartmentOfAuthenticatedHeadPersonId(Authentication authentication, Integer userId) {
        return isSamePersonId(authentication, userId) || isInDepartmentOfAuthenticatedHeadPersonId(authentication, userId);
    }

    public boolean isInDepartmentOfAuthenticatedHeadPersonId(Authentication authentication, Integer userId) {
        Optional<Person> loggedInUser = personService.getPersonByUsername(userName(authentication));
        if (loggedInUser.isEmpty() || !loggedInUser.get().hasRole(Role.DEPARTMENT_HEAD)) {
            return false;
        }
        final Optional<Person> person = personService.getPersonByID(userId);
        if (person.isEmpty()) {
            return false;
        }
        return departmentService.getManagedDepartmentsOfDepartmentHead(loggedInUser.get()).stream()
            .anyMatch(d -> d.getMembers().contains(person.get()));

    }

    public boolean isSamePersonId(Authentication authentication, Integer userId) {

        final Optional<Person> person = personService.getPersonByID(userId);
        if (person.isEmpty()) {
            return false;
        }

        String usernameToCheck = person.get().getUsername();
        return (usernameToCheck != null) && usernameToCheck.equals(userName(authentication));
    }

    private String userName(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.ldap.userdetails.Person) {
            return ((org.springframework.security.ldap.userdetails.Person) principal).getUsername();
        } else if (principal instanceof User) {
            return ((User) principal).getUsername();
        } else if (principal instanceof DefaultOidcUser) {
            return ((DefaultOidcUser) principal).getIdToken().getSubject();
        }
        return null;
    }
}
