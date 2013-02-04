package org.synyx.urlaubsverwaltung.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.PersonService;

/**
 * @author Aljona Murygina - murygina@synyx.de
 */
public class SecurityUtil {

    private PersonService personService;

    public SecurityUtil(PersonService personService) {
        this.personService = personService;
    }

    /* This method gets logged-in user and his username; with the username you get the person's ID to be able to show
    * overview of this person. Logged-in user is added to model.
    */
    public void setLoggedUser(Model model) {

        model.addAttribute(PersonConstants.LOGGED_USER, getLoggedUser());
    }


    /**
     * This method allows to get a person by logged-in user.
     *
     * @return Person that is logged in
     */
    public Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }
    
     public boolean isOffice() {
       
         if(getLoggedUser().hasRole(Role.OFFICE)) {
             return true;
         }
         
         return false;
         
     }

    public boolean isBoss() {

        if(getLoggedUser().hasRole(Role.BOSS)) {
            return true;
        }

        return false;

    }

    public boolean isAdmin() {

        if(getLoggedUser().hasRole(Role.ADMIN)) {
            return true;
        }

        return false;

    }

    public boolean isUser() {

        if(getLoggedUser().hasRole(Role.USER)) {
            return true;
        }

        return false;

    }

    public boolean isActive() {

        if(getLoggedUser().hasRole(Role.INACTIVE)) {
            return false;
        }

        return true;

    }

    public boolean isInactive() {

        if(getLoggedUser().hasRole(Role.INACTIVE)) {
            return true;
        }

        return false;

    }

}
