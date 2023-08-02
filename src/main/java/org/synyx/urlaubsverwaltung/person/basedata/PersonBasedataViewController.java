package org.synyx.urlaubsverwaltung.person.basedata;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
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
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataDtoMapper.mapToPersonBasedata;
import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataDtoMapper.mapToPersonBasedataDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonBasedataViewController implements HasLaunchpad {

    private final PersonBasedataService personBasedataService;
    private final PersonService personService;

    @Autowired
    public PersonBasedataViewController(PersonBasedataService personBasedataService, PersonService personService) {
        this.personBasedataService = personBasedataService;
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/basedata")
    public String showPersonBasedata(@PathVariable("personId") Long personId, Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final PersonBasedata personBasedata = personBasedataService.getBasedataByPersonId(personId)
            .orElse(new PersonBasedata(new PersonId(personId), "", ""));

        model.addAttribute("personBasedata", mapToPersonBasedataDto(personBasedata, person));

        return "person/person-basedata";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/basedata")
    public String editPersonBasedata(@PathVariable("personId") Long personId,
                                     @Valid @ModelAttribute("personBasedata") PersonBasedataDto basedataDto, Errors errors, Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            return "person/person-basedata";
        }

        personBasedataService.update(mapToPersonBasedata(basedataDto));

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/person/" + personId;
    }
}
