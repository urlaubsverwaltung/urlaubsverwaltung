package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonDeleteViewController {

    private final PersonService personService;

    @Autowired
    public PersonDeleteViewController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/delete")
    public String deletePerson(@PathVariable("personId") Integer personId, @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm, Errors errors, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        if (person.getPermissions().contains(OFFICE) &&
            personService.numberOfPersonsWithOfficeRoleExcludingPerson(person.getId()) == 0) {
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        if (!person.getNiceName().equals(personDeleteForm.getNiceNameConfirmation())) {
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        personService.delete(person);

        redirectAttributes.addFlashAttribute("personDeletionSuccess", person.getNiceName());
        return "redirect:/web/person/";
    }
}
