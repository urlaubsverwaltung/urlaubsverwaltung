package org.synyx.urlaubsverwaltung.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;

/**
 * Controller for managing user roles relevant stuff.
 *
 * @author Aljona Murygina
 */
@Controller
public class RoleManagementController {

    private final String JSP_FOLDER = "rolemanagement";
    private PersonService personService;
    private GravatarUtil gravatarUtil;

    public RoleManagementController(PersonService personService, GravatarUtil gravatarUtil) {
        this.personService = personService;
        this.gravatarUtil = gravatarUtil;
    }

    /**
     * Shows list with staff and which roles they have.
     *
     * @param model
     *
     * @return
     */
    @RequestMapping(value = "/management", method = RequestMethod.GET)
    public String showStaffWithRoles(Model model) {

        if (getLoggedUser().getRole() == Role.ADMIN) {
            setLoggedUser(model);

            List<Person> persons = personService.getAllPersons();
            persons.addAll(personService.getInactivePersons());

            if (persons.isEmpty()) {
                model.addAttribute(PersonConstants.NOTEXISTENT, true);
                // if nothing to show, display a message
            } else {
                // if there are persons, prepare the persons list
                prepareList(persons, model);
            }

            return JSP_FOLDER + "/list";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

    private void prepareList(List<Person> persons, Model model) {

        Map<Person, String> gravatarUrls = new HashMap<Person, String>();
        String url;

        for (Person person : persons) {
            // get url of person's gravatar image
            url = gravatarUtil.createImgURL(person.getEmail());

            if (url != null) {
                gravatarUrls.put(person, url);
            }

        }

        model.addAttribute(ControllerConstants.PERSONS, persons);
        model.addAttribute(PersonConstants.GRAVATAR_URLS, gravatarUrls);
    }

    @RequestMapping(value = "/management/{" + PersonConstants.PERSON_ID + "}", method = RequestMethod.GET)
    public String editRoleForm(HttpServletRequest request,
            @PathVariable(PersonConstants.PERSON_ID) Integer personId, Model model) {

        if (getLoggedUser().getRole() == Role.ADMIN) {
            Person person = personService.getPersonByID(personId);

            setLoggedUser(model);
            model.addAttribute(ControllerConstants.PERSON, person);
            model.addAttribute("roles", Role.values());

            return JSP_FOLDER + "/role_edit";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

    @RequestMapping(value = "/management/{" + PersonConstants.PERSON_ID + "}", method = RequestMethod.PUT)
    public String editRole(HttpServletRequest request,
            @PathVariable(PersonConstants.PERSON_ID) Integer personId,
            @ModelAttribute("person") Person person, Model model) {

        if (getLoggedUser().getRole() == Role.ADMIN) {

            Person personToSave = personService.getPersonByID(personId);
            personToSave.setActive(person.isActive());
            personToSave.setRole(person.getRole());
            personService.save(personToSave);

            return "redirect:/web/management/";

        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

    /*
     * This method gets logged-in user and his username; with the username you get the person's ID to be able to show
     * overview of this person. Logged-in user is added to model.
     */
    private void setLoggedUser(Model model) {

        model.addAttribute(PersonConstants.LOGGED_USER, getLoggedUser());
    }

    /**
     * This method allows to get a person by logged-in user.
     *
     * @return Person that is logged in
     */
    private Person getLoggedUser() {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        return personService.getPersonByLogin(user);
    }
}
