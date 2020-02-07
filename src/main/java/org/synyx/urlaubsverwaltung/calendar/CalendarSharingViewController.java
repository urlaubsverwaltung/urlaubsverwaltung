package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;


@Controller
@RequestMapping("/web/persons/{personId}/calendar/share")
public class CalendarSharingViewController {

    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    public CalendarSharingViewController(PersonCalendarService personCalendarService, DepartmentCalendarService departmentCalendarService,
                                               PersonService personService, DepartmentService departmentService) {
        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.personService = personService;
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String index(@PathVariable int personId, Model model, HttpServletRequest request) {

        final PersonCalendarDto dto = getPersonCalendarDto(personId, request);
        model.addAttribute("privateCalendarShare", dto);

        final List<DepartmentCalendarDto> departmentCalendarDtos = getDepartmentCalendarDtos(personId, request);
        model.addAttribute("departmentCalendars", departmentCalendarDtos);

        return "calendarsharing/index";
    }

    @PostMapping(value = "/me")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.createCalendarForPerson(personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }

    @PostMapping(value = "/me", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.deletePersonalCalendarForPerson(personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }

    @PostMapping(value = "/departments/{departmentId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkDepartmentCalendar(@PathVariable int personId, @PathVariable int departmentId) {

        departmentCalendarService.createCalendarForDepartmentAndPerson(departmentId, personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }

    @PostMapping(value = "/departments/{departmentId}", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkDepartmentCalendar(@PathVariable int personId, @PathVariable int departmentId) {

        departmentCalendarService.deleteCalendarForDepartmentAndPerson(departmentId, personId);

        return format("redirect:/web/persons/%d/calendar/share", personId);
    }

    private PersonCalendarDto getPersonCalendarDto(@PathVariable int personId, HttpServletRequest request) {
        final PersonCalendarDto dto = new PersonCalendarDto();
        dto.setPersonId(personId);

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarService.getPersonCalendar(personId);
        if (maybePersonCalendar.isPresent()) {
            final PersonCalendar personCalendar = maybePersonCalendar.get();
            final String url = format("%s://%s/web/persons/%d/calendar?secret=%s",
                request.getScheme(), request.getHeader("host"), personId, personCalendar.getSecret());

            dto.setCalendarUrl(url);
        }

        return dto;
    }

    private List<DepartmentCalendarDto> getDepartmentCalendarDtos(int personId, HttpServletRequest request) {

        final Person person = getPersonOrThrow(personId);
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        final List<DepartmentCalendarDto> departmentCalendarDtos = new ArrayList<>(departments.size());

        for (Department department : departments) {

            final var departmentId = department.getId();
            final var departmentCalendarDto = new DepartmentCalendarDto();
            departmentCalendarDto.setDepartmentId(departmentId);
            departmentCalendarDto.setPersonId(personId);
            departmentCalendarDto.setDepartmentName(department.getName());

            final var maybeCalendar = departmentCalendarService.getCalendarForDepartment(departmentId, personId);
            if (maybeCalendar.isPresent()) {
                final var departmentCalendar = maybeCalendar.get();
                final var url = format("%s://%s/web/departments/%s/persons/%s/calendar?secret=%s",
                    request.getScheme(), request.getHeader("host"), departmentId, personId, departmentCalendar.getSecret());

                departmentCalendarDto.setCalendarUrl(url);
            }

            departmentCalendarDtos.add(departmentCalendarDto);
        }

        return departmentCalendarDtos;
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
