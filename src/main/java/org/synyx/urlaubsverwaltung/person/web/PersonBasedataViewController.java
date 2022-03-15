package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.synyx.urlaubsverwaltung.person.web.PersonBasedataDtoMapper.mapToPersonBasedataDto;
import static org.synyx.urlaubsverwaltung.person.web.PersonBasedataDtoMapper.merge;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonBasedataViewController {

    private final PersonService personService;

    @Autowired
    public PersonBasedataViewController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/basedata")
    public String showPersonBasedata(@PathVariable("personId") Integer personId, Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute("person", mapToPersonBasedataDto(person));

        return "person/person_basedata";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/basedata")
    public String editPersonBasedata(@PathVariable("personId") Integer personId,
                                     @ModelAttribute("person") PersonBasedataDto masterdataDto, Errors errors, RedirectAttributes redirectAttributes) throws UnknownPersonException {

        if (errors.hasErrors()) {
            return "person_basedata";
        }

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        personService.update(merge(person, masterdataDto));

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/person/" + personId;
    }
}
