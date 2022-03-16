package org.synyx.urlaubsverwaltung.person.basedata;

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
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.function.Supplier;

import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataDtoMapper.mapToPersonBasedata;
import static org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataDtoMapper.mapToPersonBasedataDto;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web")
public class PersonBasedataViewController {

    private final PersonBasedataService personBasedataService;
    private final PersonService personService;

    @Autowired
    public PersonBasedataViewController(PersonBasedataService personBasedataService, PersonService personService) {
        this.personBasedataService = personBasedataService;
        this.personService = personService;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/person/{personId}/basedata")
    public String showPersonBasedata(@PathVariable("personId") Integer personId, Model model) {

        final PersonBasedata personBasedata = personBasedataService.getBasedataByPersonId(personId)
            .orElseGet(getPersonDataOnly(personId));
        model.addAttribute("personBasedata", mapToPersonBasedataDto(personBasedata));

        return "person/person_basedata";
    }

    @PreAuthorize(IS_OFFICE)
    @PostMapping("/person/{personId}/basedata")
    public String editPersonBasedata(@PathVariable("personId") Integer personId,
                                     @ModelAttribute("person") PersonBasedataDto basedataDto, Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            return "person_basedata";
        }

        personBasedataService.update(mapToPersonBasedata(basedataDto));

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/person/" + personId;
    }

    private Supplier<PersonBasedata> getPersonDataOnly(Integer personId) {
        return () -> {
            final PersonBasedata.Builder personBasedataBuilder = PersonBasedata.builder();
            personService.getPersonByID(personId).ifPresent(person -> personBasedataBuilder
                .withPersonId(person.getId())
                .withNiceName(person.getNiceName())
                .withGravatarURL(person.getGravatarURL())
                .withEmail(person.getEmail()));
            return personBasedataBuilder.build();
        };
    }
}
