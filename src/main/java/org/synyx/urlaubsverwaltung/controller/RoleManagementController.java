package org.synyx.urlaubsverwaltung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.PersonService;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private SecurityUtil securityUtil;

    public RoleManagementController(PersonService personService, GravatarUtil gravatarUtil, SecurityUtil securityUtil) {
        this.personService = personService;
        this.gravatarUtil = gravatarUtil;
        this.securityUtil = securityUtil;
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

        if (securityUtil.isAdmin()) {
            securityUtil.setLoggedUser(model);

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

        if (securityUtil.isAdmin()) {
            Person person = personService.getPersonByID(personId);

            securityUtil.setLoggedUser(model);
            model.addAttribute(ControllerConstants.PERSON, person);

            return JSP_FOLDER + "/role_edit";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

    @RequestMapping(value = "/management/{" + PersonConstants.PERSON_ID + "}", method = RequestMethod.PUT)
    public String editRole(HttpServletRequest request,
            @PathVariable(PersonConstants.PERSON_ID) Integer personId,
            @ModelAttribute("person") Person person, Model model) {

        if (securityUtil.isAdmin()) {

            Person personToSave = personService.getPersonByID(personId);
            personService.editPermissions(personToSave, person.isActive(), person.getPermissions());

            return "redirect:/web/management/";

        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }

}
