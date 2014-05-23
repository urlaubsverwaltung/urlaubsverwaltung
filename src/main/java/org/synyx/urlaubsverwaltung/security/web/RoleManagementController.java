package org.synyx.urlaubsverwaltung.security.web;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.web.PersonConstants;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.util.GravatarUtil;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * Controller for managing user roles relevant stuff.
 *
 * @author  Aljona Murygina
 */
@Controller
public class RoleManagementController {

    private final String JSP_FOLDER = "rolemanagement";

    @Autowired
    private PersonService personService;

    @Autowired
    private SessionService sessionService;

    /**
     * Shows list with staff and which roles they have.
     *
     * @param  model
     *
     * @return
     */
    @RequestMapping(value = "/management", method = RequestMethod.GET)
    public String showStaffWithRoles(Model model) {

        if (sessionService.isAdmin()) {
            sessionService.setLoggedUser(model);

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
            url = GravatarUtil.createImgURL(person.getEmail());

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

        if (sessionService.isAdmin()) {
            prepareModel(model, personService.getPersonByID(personId));

            return JSP_FOLDER + "/role_edit";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    @RequestMapping(value = "/management/{" + PersonConstants.PERSON_ID + "}", method = RequestMethod.PUT)
    public String editRole(HttpServletRequest request,
        @PathVariable(PersonConstants.PERSON_ID) Integer personId,
        @ModelAttribute("person") Person person, Model model) {

        if (sessionService.isAdmin()) {
            Person personToSave = personService.getPersonByID(personId);

            String msg = validatePermissions(person);

            if (StringUtils.hasText(msg)) {
                prepareModel(model, personToSave);
                model.addAttribute("msg", msg);

                return JSP_FOLDER + "/role_edit";
            }

            personService.editPermissions(personToSave, person.getPermissions());

            return "redirect:/web/management/";
        } else {
            return ControllerConstants.ERROR_JSP;
        }
    }


    private String validatePermissions(Person person) {

        String msg = "";

        Collection<Role> roles = person.getPermissions();

        if (roles == null || roles.isEmpty()) {
            msg = "role.error.least";
        } else {
            // if role inactive set, then only this role may be selected
            // else this is an error

            boolean roleInactiveSet = false;

            for (Role r : roles) {
                if (r == Role.INACTIVE) {
                    roleInactiveSet = true;
                }
            }

            if (roleInactiveSet) {
                // validate that there is only role inactive set
                // this means size of role collection must have size 1
                if (roles.size() != 1) {
                    msg = "role.error.inactive";
                }
            }
        }

        return msg;
    }


    private void prepareModel(Model model, Person person) {

        sessionService.setLoggedUser(model);
        model.addAttribute(ControllerConstants.PERSON, person);
    }
}
