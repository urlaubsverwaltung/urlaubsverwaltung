package org.synyx.urlaubsverwaltung.person.web;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettings;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToMailNotifications;
import static org.synyx.urlaubsverwaltung.person.web.PersonNotificationsMapper.mapToPersonNotificationsDto;

@Controller
@RequestMapping("/web")
public class PersonNotificationsViewController implements HasLaunchpad {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    public static final String ACTIVE_CONTENT = "activeContent";
    public static final String ACTIVE_CONTENT_SELF = "self";
    public static final String ACTIVE_CONTENT_DEPARTMENTS = "departments";

    private final PersonService personService;
    private final PersonNotificationsDtoValidator validator;
    private final UserNotificationSettingsService userNotificationSettingsService;
    private final DepartmentService departmentService;

    @Autowired
    PersonNotificationsViewController(PersonService personService,
                                      PersonNotificationsDtoValidator validator,
                                      UserNotificationSettingsService userNotificationSettingsService,
                                      DepartmentService departmentService) {

        this.personService = personService;
        this.validator = validator;
        this.userNotificationSettingsService = userNotificationSettingsService;
        this.departmentService = departmentService;
    }

    @GetMapping("/person/{personId}/notifications")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String showPersonNotifications(@PathVariable int personId, Model model) throws UnknownPersonException {
        return showNotifications(false, personId, model);
    }

    @GetMapping("/person/{personId}/notifications/departments")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String showDepartmentNotifications(@PathVariable int personId, Model model) throws UnknownPersonException {
        return showNotifications(true, personId, model);
    }

    private String showNotifications(boolean isDepartmentSection, int personId, Model model) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        final Person signedInUser = personService.getSignedInUser();
        model.addAttribute("isViewingOwnNotifications", Objects.equals(person.getId(), signedInUser.getId()));
        model.addAttribute("personNiceName", person.getNiceName());

        final PersonNotificationsDto personNotificationsDto = mapToPersonNotificationsDto(person);

        final UserNotificationSettings notificationSettings = userNotificationSettingsService.findNotificationSettings(new PersonId(person.getId()));
        personNotificationsDto.setRestrictToDepartments(notificationSettings.isRestrictToDepartments());

        final List<Department> signedInUserDepartments = departmentService.getDepartmentsPersonHasAccessTo(signedInUser);

        model.addAttribute("personNotificationsDto", personNotificationsDto);
        model.addAttribute("showDepartmentsTab", !signedInUserDepartments.isEmpty());

        if (isDepartmentSection) {
            model.addAttribute("formFragment", "person/notifications/departments::form");
            model.addAttribute(ACTIVE_CONTENT, ACTIVE_CONTENT_DEPARTMENTS);
        } else {
            model.addAttribute("formFragment", "person/notifications/self::form");
            model.addAttribute(ACTIVE_CONTENT, ACTIVE_CONTENT_SELF);
        }

        return "person/person_notifications";
    }

    @PostMapping("/person/{personId}/notifications")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String editSelfNotifications(@PathVariable int personId,
                                        @ModelAttribute PersonNotificationsDto newPersonNotificationsDto,
                                        Errors errors,
                                        Model model,
                                        RedirectAttributes redirectAttributes) throws UnknownPersonException {
        return editNotifications(false, personId, newPersonNotificationsDto, errors, model, redirectAttributes, ACTIVE_CONTENT_SELF);
    }

    @PostMapping("/person/{personId}/notifications/departments")
    @PreAuthorize("hasAuthority('OFFICE') or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String editDepartmentsNotifications(@PathVariable int personId,
                                               @ModelAttribute PersonNotificationsDto newPersonNotificationsDto,
                                               Errors errors,
                                               Model model,
                                               RedirectAttributes redirectAttributes) throws UnknownPersonException {
        return editNotifications(true, personId, newPersonNotificationsDto, errors, model, redirectAttributes, ACTIVE_CONTENT_DEPARTMENTS);
    }


    private String editNotifications(boolean isDepartmentSection, int personId, PersonNotificationsDto newPersonNotificationsDto, Errors errors,
                                     Model model, RedirectAttributes redirectAttributes, String section) throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId)
            .orElseThrow(() -> new UnknownPersonException(personId));

        if (!person.getId().equals(newPersonNotificationsDto.getPersonId())) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        validator.validate(newPersonNotificationsDto, errors);
        if (errors.hasErrors()) {
            LOG.error("Could not save e-mail-notifications of user {}", person.getId());

            final PersonNotificationsDto mergedPersonNotificationsDto = new PersonNotificationsDto();
            BeanUtils.copyProperties(mapToPersonNotificationsDto(person), mergedPersonNotificationsDto);
            model.addAttribute("personNotificationsDto", mergedPersonNotificationsDto);
            model.addAttribute("error", true);

            if (isDepartmentSection) {
                model.addAttribute("formFragment", "person/notifications/departments::form");
                model.addAttribute(ACTIVE_CONTENT, ACTIVE_CONTENT_DEPARTMENTS);
            } else {
                model.addAttribute("formFragment", "person/notifications/self::form");
                model.addAttribute(ACTIVE_CONTENT, ACTIVE_CONTENT_SELF);
            }

            return "person/person_notifications";
        }

        person.setNotifications(mapToMailNotifications(newPersonNotificationsDto));

        personService.update(person);
        userNotificationSettingsService.updateNotificationSettings(new PersonId(person.getId()), newPersonNotificationsDto.isRestrictToDepartments());

        redirectAttributes.addFlashAttribute("success", true);

        if (hasText(section) && !section.equals(ACTIVE_CONTENT_SELF)) {
            return format("redirect:/web/person/%s/notifications/%s", person.getId(), section);
        } else {
            return format("redirect:/web/person/%s/notifications", person.getId());
        }
    }
}
