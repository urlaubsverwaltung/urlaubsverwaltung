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

import static java.lang.String.format;
import static java.util.List.copyOf;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapRoleToPermissionsDto;

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

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();
        personNotificationsDto.setId(personId);
        personNotificationsDto.setName(person.getFirstName());
        personNotificationsDto.setEmailNotifications(copyOf(person.getNotifications()));
        personNotificationsDto.setPermissions(mapRoleToPermissionsDto(copyOf(person.getPermissions())));
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

        if (!person.getId().equals(personNotificationsDto.getId())) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        validator.validate(personNotificationsDto, errors);
        if (errors.hasErrors()) {
            return "person/person_notifications";
        }

        person.setNotifications(personNotificationsDto.getEmailNotifications());
        personService.update(person);

        return format("redirect:/web/person/%s/notifications", personId);
    }
}
