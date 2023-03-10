package org.synyx.urlaubsverwaltung.person.web;

import org.springframework.beans.factory.annotation.Autowired;
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

import static java.lang.String.format;
import static java.util.List.copyOf;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.merge;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsMapper.mapRoleToPermissionsDto;

@Controller
@RequestMapping("/web")
public class PersonNotificationsViewController {

    private final PersonService personService;
    private final PersonNotificationsDtoValidator validator;

    @Autowired
    public PersonNotificationsViewController(PersonService personService, PersonNotificationsDtoValidator validator) {
        this.personService = personService;
        this.validator = validator;
    }

    @GetMapping("/person/{personId}/notifications")
    public String showPersonPermissionsAndNotifications(@PathVariable("personId") Integer personId, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId)) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final PersonNotificationsDto personNotificationsDto = new PersonNotificationsDto();
        personNotificationsDto.setId(personId);
        personNotificationsDto.setName(signedInUser.getFirstName());
        personNotificationsDto.setEmailNotifications(copyOf(signedInUser.getNotifications()));
        personNotificationsDto.setPermissions(mapRoleToPermissionsDto(copyOf(signedInUser.getPermissions())));
        model.addAttribute("personNotificationsDto", personNotificationsDto);

        return "person/person_notifications";
    }

    @PostMapping("/person/{personId}/notifications")
    public String editPersonPermissionsAndNotifications(@PathVariable("personId") Integer personId,
                                                        @ModelAttribute PersonNotificationsDto personNotificationsDto, Errors errors) {

        final Person signedInUser = personService.getSignedInUser();
        if (!signedInUser.getId().equals(personId) || !signedInUser.getId().equals(personNotificationsDto.getId())) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        validator.validate(personNotificationsDto, errors);
        if (errors.hasErrors()) {
            return "person/person_notifications";
        }

        personService.update(merge(signedInUser, personNotificationsDto));

        return format("redirect:/web/person/%s/notifications", personId);
    }
}
