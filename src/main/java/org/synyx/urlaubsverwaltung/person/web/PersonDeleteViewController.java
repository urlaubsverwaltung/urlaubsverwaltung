package org.synyx.urlaubsverwaltung.person.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonDeleteViewController implements HasLaunchpad {

    private final PersonService personService;

    @Autowired
    public PersonDeleteViewController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = "/person/{personId}/delete", params = {"delete"})
    public String deletePerson(
        @PathVariable("personId") Long personId,
        @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm,
        RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person personToDelete = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        if (isLastOfficeUser(personToDelete)) {
            redirectAttributes.addFlashAttribute("lastOfficeUserCannotBeDeleted", true);
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        redirectAttributes.addFlashAttribute("firstDeleteActionConfirmed", true);
        return "redirect:/web/person/{personId}#person-delete-form";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = "/person/{personId}/delete", params = {"delete"}, headers = {"Turbo-Frame"})
    public String deletePersonAjax(
        @PathVariable("personId") Long personId,
        @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm,
        @RequestHeader(name = "Turbo-Frame") String turboFrame,
        Model model) throws UnknownPersonException {

        final Person personToDelete = getPerson(personId);

        final boolean lastOfficeUserCannotBeDeleted;
        final boolean firstDeleteActionConfirmed;

        if (isLastOfficeUser(personToDelete)) {
            lastOfficeUserCannotBeDeleted = true;
            firstDeleteActionConfirmed = false;
        } else {
            lastOfficeUserCannotBeDeleted = false;
            firstDeleteActionConfirmed = true;
        }

        model.addAttribute("person", personToDelete);
        model.addAttribute("lastOfficeUserCannotBeDeleted", lastOfficeUserCannotBeDeleted);
        model.addAttribute("firstDeleteActionConfirmed", firstDeleteActionConfirmed);

        return "person/detail-section/action-delete-person :: #" + turboFrame;
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/delete")
    public String deletePersonConfirmed(@PathVariable("personId") Long personId, @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        final Person personToDelete = getPerson(personId);

        if (isLastOfficeUser(personToDelete)) {
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        if (!deleteConfirmationMatch(personToDelete, personDeleteForm)) {
            redirectAttributes.addFlashAttribute("firstDeleteActionConfirmed", true);
            redirectAttributes.addFlashAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch");
            return "redirect:/web/person/{personId}#person-delete-form";
        }

        return deletePerson(personToDelete, redirectAttributes);
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping(value = "/person/{personId}/delete", headers = {"Turbo-Frame"})
    public String deletePersonConfirmedAjax(
        @PathVariable("personId") Long personId,
        @ModelAttribute("personDeleteForm") PersonDeleteForm personDeleteForm,
        @RequestHeader("Turbo-Frame") String turboFrame,
        RedirectAttributes redirectAttributes, Model model) throws UnknownPersonException {

        final Person personToDelete = getPerson(personId);

        if (isLastOfficeUser(personToDelete)) {
            model.addAttribute("person", personToDelete);
            model.addAttribute("lastOfficeUserCannotBeDeleted", true);
            model.addAttribute("firstDeleteActionConfirmed", true);
            model.addAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office");
            return "person/detail-section/action-delete-person :: #" + turboFrame;
        }

        if (!deleteConfirmationMatch(personToDelete, personDeleteForm)) {
            model.addAttribute("person", personToDelete);
            model.addAttribute("firstDeleteActionConfirmed", true);
            model.addAttribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch");
            return "person/detail-section/action-delete-person :: #" + turboFrame;
        }

        return deletePerson(personToDelete, redirectAttributes);
    }

    private Person getPerson(Long personId) throws UnknownPersonException {
        return personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
    }

    private boolean isLastOfficeUser(Person person) {
        return person.getPermissions().contains(OFFICE) &&
            personService.numberOfPersonsWithOfficeRoleExcludingPerson(person.getId()) == 0;
    }

    private boolean deleteConfirmationMatch(Person person, PersonDeleteForm personDeleteForm) {
        return person.getNiceName().equals(personDeleteForm.getNiceNameConfirmation());
    }

    private String deletePerson(Person person, RedirectAttributes redirectAttributes) {
        final boolean isActive = person.isActive();

        personService.delete(person, personService.getSignedInUser());

        redirectAttributes.addFlashAttribute("personDeletionSuccess", person.getNiceName());

        return "redirect:/web/person?active=" + isActive;
    }
}
