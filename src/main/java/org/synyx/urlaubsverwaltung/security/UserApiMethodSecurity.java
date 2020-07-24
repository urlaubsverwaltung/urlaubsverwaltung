package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@Component
public class UserApiMethodSecurity {

    private PersonService personService;
    private DepartmentService departmentService;

    @Autowired
    public UserApiMethodSecurity(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    public boolean isInDepartmentOfDepartmentHead(Authentication authentication, Integer userId) {
        final Optional<Person> loggedInUser = personService.getPersonByUsername(userName(authentication));

        if (loggedInUser.isEmpty() || !loggedInUser.get().hasRole(DEPARTMENT_HEAD)) {
            return false;
        }

        final Optional<Person> person = personService.getPersonByID(userId);
        if (person.isEmpty()) {
            return false;
        }

        final List<Department> departmentsOfDepartmentHead = departmentService.getManagedDepartmentsOfDepartmentHead(loggedInUser.get());
        return departmentsOfDepartmentHead.stream().anyMatch(d -> d.getMembers().contains(person.get()));
    }

    public boolean isSamePersonId(Authentication authentication, Integer userId) {

        final Optional<Person> person = personService.getPersonByID(userId);
        if (person.isEmpty()) {
            return false;
        }

        final String usernameToCheck = person.get().getUsername();
        return (usernameToCheck != null) && usernameToCheck.equals(userName(authentication));
    }

    private String userName(Authentication authentication) {
        String username = null;
        final Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.ldap.userdetails.Person) {
            username = ((org.springframework.security.ldap.userdetails.Person) principal).getUsername();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else if (principal instanceof DefaultOidcUser) {
            username = ((DefaultOidcUser) principal).getIdToken().getSubject();
        }
        return username;
    }
}
