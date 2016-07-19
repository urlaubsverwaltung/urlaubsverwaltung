
package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;

import java.math.BigDecimal;

import java.util.Locale;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class PersonManagementController {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonValidator validator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/new", method = RequestMethod.GET)
    public String newPersonForm(Model model) {

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, new Person());

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff", method = RequestMethod.POST)
    public String newPerson(@ModelAttribute(PersonConstants.PERSON_ATTRIBUTE) Person person, Errors errors,
        RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PersonConstants.PERSON_FORM_JSP;
        }

        Person createdPerson = personService.create(person);

        redirectAttributes.addFlashAttribute("createSuccess", true);

        return "redirect:/web/staff/" + createdPerson.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/data", method = RequestMethod.GET)
    public String editPersonData(@PathVariable("personId") Integer personId, Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/staff/{personId}/data", method = RequestMethod.POST)
    public String editPersonData(@PathVariable("personId") Integer personId,
        @ModelAttribute(PersonConstants.PERSON_ATTRIBUTE) Person person, Errors errors,
        RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PersonConstants.PERSON_FORM_JSP;
        }

        personService.update(person);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
