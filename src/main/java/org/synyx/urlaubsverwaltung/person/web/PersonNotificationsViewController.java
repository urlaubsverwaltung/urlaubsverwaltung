package org.synyx.urlaubsverwaltung.person.web;

import de.focusshift.launchpad.api.HasLaunchpad;
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
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.util.Objects;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToMailNotifications;

@Controller
@RequestMapping("/web")
public class PersonNotificationsViewController implements HasLaunchpad {

    private final PersonService personService;
    private final PersonNotificationsDtoValidator validator;

    @Autowired
    public PersonNotificationsViewController(PersonService personService, PersonNotificationsDtoValidator validator) {
        this.personService = personService;
        this.validator = validator;
    }

    @GetMapping("/person/{personId}/notifications")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String showPersonNotifications(@PathVariable int personId, Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("isViewingOwnNotifications", Objects.equals(person.getId(), signedInUser.getId()));
        model.addAttribute("personNiceName", person.getNiceName());

        final PersonNotificationsDto personNotificationsDto = PersonNotificationsMapper.mapToPersonNotificationsDto(person);
        model.addAttribute("personNotificationsDto", personNotificationsDto);

        return "person/person_notifications";
    }

    @PostMapping("/person/{personId}/notifications")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String editPersonNotifications(@PathVariable int personId,
                                          @ModelAttribute PersonNotificationsDto personNotificationsDto,
                                          Errors errors) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        if (!person.getId().equals(personNotificationsDto.getPersonId())) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        validator.validate(personNotificationsDto, errors);
        if (errors.hasErrors()) {
            return "person/person_notifications";
        }

        person.setNotifications(mapToMailNotifications(personNotificationsDto));

        personService.update(person);

        return format("redirect:/web/person/%s/notifications", person.getId());
    }
}
