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
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;


@Controller
@RequestMapping("/web/calendars/share/persons/{personId}")
public class CalendarSharingViewController {

    private static final String REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D = "redirect:/web/calendars/share/persons/%d";
    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CompanyCalendarService companyCalendarService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final CalendarAccessibleService calendarAccessibleService;

    @Autowired
    public CalendarSharingViewController(PersonCalendarService personCalendarService, DepartmentCalendarService departmentCalendarService,
                                         CompanyCalendarService companyCalendarService, PersonService personService, DepartmentService departmentService, CalendarAccessibleService calendarAccessibleService) {
        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
        this.personService = personService;
        this.departmentService = departmentService;
        this.calendarAccessibleService = calendarAccessibleService;
    }

    @GetMapping
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String index(@PathVariable int personId, Model model, HttpServletRequest request) {

        final PersonCalendarDto dto = getPersonCalendarDto(personId, request);
        model.addAttribute("privateCalendarShare", dto);

        final List<DepartmentCalendarDto> departmentCalendarDtos = getDepartmentCalendarDtos(personId, request);
        model.addAttribute("departmentCalendars", departmentCalendarDtos);

        final Person signedInUser = personService.getSignedInUser();
        final boolean isBossOrOffice = signedInUser.hasRole(Role.OFFICE) || signedInUser.hasRole(Role.BOSS);
        final boolean companyCalendarAccessible = calendarAccessibleService.isCompanyCalendarAccessible();

        if (isBossOrOffice) {
            // feature: enable / disable companyCalendar
            final CompanyCalendarAccessibleDto companyCalendarAccessibleDto = new CompanyCalendarAccessibleDto();
            companyCalendarAccessibleDto.setAccessible(companyCalendarAccessible);
            model.addAttribute("companyCalendarAccessible", companyCalendarAccessibleDto);
        }

        if (isBossOrOffice || companyCalendarAccessible) {
            // feature: create / delete link for companyCalendar
            final CompanyCalendarDto companyCalendarDto = getCompanyCalendarDto(personId, request);
            model.addAttribute("companyCalendarShare", companyCalendarDto);
        }

        return "calendarsharing/index";
    }

    @GetMapping("/departments/{activeDepartmentId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String indexDepartment(@PathVariable int personId, @PathVariable int activeDepartmentId, Model model, HttpServletRequest request) {

        final PersonCalendarDto dto = getPersonCalendarDto(personId, request);
        model.addAttribute("privateCalendarShare", dto);

        final List<DepartmentCalendarDto> departmentCalendarDtos = getDepartmentCalendarDtos(personId, activeDepartmentId, request);
        model.addAttribute("departmentCalendars", departmentCalendarDtos);

        return "calendarsharing/index";
    }

    @PostMapping(value = "/me")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.createCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D, personId);
    }

    @PostMapping(value = "/me", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkPrivateCalendar(@PathVariable int personId) {

        personCalendarService.deletePersonalCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D, personId);
    }

    @PostMapping(value = "/departments/{departmentId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkDepartmentCalendar(@PathVariable int personId, @PathVariable int departmentId) {

        departmentCalendarService.createCalendarForDepartmentAndPerson(departmentId, personId);

        return format("redirect:/web/calendars/share/persons/%d/departments/%d", personId, departmentId);
    }

    @PostMapping(value = "/departments/{departmentId}", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkDepartmentCalendar(@PathVariable int personId, @PathVariable int departmentId) {

        departmentCalendarService.deleteCalendarForDepartmentAndPerson(departmentId, personId);

        return format("redirect:/web/calendars/share/persons/%d/departments/%d", personId, departmentId);
    }

    @PostMapping(value = "/company")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkCompanyCalendar(@PathVariable int personId) {

        companyCalendarService.createCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D, personId);
    }

    @PostMapping(value = "/company", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkCompanyCalendar(@PathVariable int personId) {

        companyCalendarService.deleteCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D, personId);
    }

    @PostMapping(value = "/company/accessible")
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public String editCompanyCalendarAccessible(@PathVariable int personId, CompanyCalendarAccessibleDto companyCalendarAccessibleDto) {

        if (companyCalendarAccessibleDto.isAccessible()) {
            calendarAccessibleService.enableCompanyCalendar();
        } else {
            calendarAccessibleService.disableCompanyCalendar();
        }

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS_D, personId);
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

        return getDepartmentCalendarDtos(personId, null, request);
    }

    private List<DepartmentCalendarDto> getDepartmentCalendarDtos(int personId, @Nullable Integer activeDepartmentId, HttpServletRequest request) {

        final Person person = getPersonOrThrow(personId);
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        final List<DepartmentCalendarDto> departmentCalendarDtos = new ArrayList<>(departments.size());

        if (activeDepartmentId != null && departments.stream().noneMatch(department -> department.getId().equals(activeDepartmentId))) {
            throw new ResponseStatusException(BAD_REQUEST, String.format(
                "person=%s is not a member of department=%s", personId, activeDepartmentId));
        }

        for (Department department : departments) {

            final var departmentId = department.getId();
            final var departmentCalendarDto = new DepartmentCalendarDto();
            departmentCalendarDto.setDepartmentId(departmentId);
            departmentCalendarDto.setPersonId(personId);
            departmentCalendarDto.setDepartmentName(department.getName());
            departmentCalendarDto.setActive(departmentId.equals(activeDepartmentId));

            final var maybeDepartmentCalendar = departmentCalendarService.getCalendarForDepartment(departmentId, personId);
            if (maybeDepartmentCalendar.isPresent()) {
                final var departmentCalendar = maybeDepartmentCalendar.get();
                final var url = format("%s://%s/web/departments/%s/persons/%s/calendar?secret=%s",
                    request.getScheme(), request.getHeader("host"), departmentId, personId, departmentCalendar.getSecret());

                departmentCalendarDto.setCalendarUrl(url);
            }

            departmentCalendarDtos.add(departmentCalendarDto);
        }

        if (!departmentCalendarDtos.isEmpty() && departmentCalendarDtos.stream().noneMatch(DepartmentCalendarDto::isActive)) {
            departmentCalendarDtos.get(0).setActive(true);
        }

        return departmentCalendarDtos;
    }

    private CompanyCalendarDto getCompanyCalendarDto(@PathVariable int personId, HttpServletRequest request) {

        final CompanyCalendarDto companyCalendarDto = new CompanyCalendarDto();
        companyCalendarDto.setPersonId(personId);

        final Optional<CompanyCalendar> maybeCompanyCalendar = companyCalendarService.getCompanyCalendar(personId);
        if (maybeCompanyCalendar.isPresent()) {
            final CompanyCalendar companyCalendar = maybeCompanyCalendar.get();
            final String url = format("%s://%s/web/company/persons/%d/calendar?secret=%s",
                request.getScheme(), request.getHeader("host"), personId, companyCalendar.getSecret());

            companyCalendarDto.setCalendarUrl(url);
        }

        return companyCalendarDto;
    }

    private Person getPersonOrThrow(Integer personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
