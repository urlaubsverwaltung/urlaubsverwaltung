package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
    @PostMapping(value = "/person/{personId}/delete", params = {"delete"})
    public String deletePerson(
        @PathVariable("personId") Integer personId,
        @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm,
        RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person personToDelete = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        if (personToDelete.getPermissions().contains(OFFICE) && personService.numberOfPersonsWithOfficeRoleExcludingPerson(personToDelete.getId()) == 0) {
            redirectAttributes.addFlashAttribute("lastOfficeUserCannotBeDeleted", true);
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        redirectAttributes.addFlashAttribute("firstDeleteActionConfirmed", true);
        return "redirect:/web/person/{personId}#person-delete-form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/delete")
    public String deletePersonConfirmed(@PathVariable("personId") Integer personId, @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person personToDelete = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        if (personToDelete.getPermissions().contains(OFFICE) &&
            personService.numberOfPersonsWithOfficeRoleExcludingPerson(personToDelete.getId()) == 0) {
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        if (!personToDelete.getNiceName().equals(personDeleteForm.getNiceNameConfirmation())) {
            redirectAttributes.addFlashAttribute("firstDeleteActionConfirmed", true);
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        final boolean isActive = personToDelete.isActive();

        personService.delete(personToDelete, personService.getSignedInUser());

        redirectAttributes.addFlashAttribute("personDeletionSuccess", personToDelete.getNiceName());
        return "redirect:/web/person/?active=" + isActive;
    }
}
