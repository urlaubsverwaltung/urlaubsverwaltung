package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.synyx.urlaubsverwaltung.person.web.PersonMasterdataDtoMapper.mapToPersonMasterdataDto;
import static org.synyx.urlaubsverwaltung.person.web.PersonMasterdataDtoMapper.merge;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonMasterataViewController {

    private final PersonService personService;

    @Autowired
    public PersonMasterataViewController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/masterdata")
    public String showPersonBasedata(@PathVariable("personId") Integer personId, Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute("person", mapToPersonMasterdataDto(person));

        return "person/person_masterdata";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/masterdata")
    public String editPersonBasedata(@PathVariable("personId") Integer personId,
                                     @ModelAttribute("person") PersonMasterdataDto masterdataDto, Errors errors, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        if (errors.hasErrors()) {
            return "person/person_masterdata";
        }

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        personService.update(merge(person, masterdataDto));

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/person/" + personId;
    }
}
