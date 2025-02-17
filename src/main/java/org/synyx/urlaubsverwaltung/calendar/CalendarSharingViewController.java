package org.synyx.urlaubsverwaltung.calendar;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Controller
@RequestMapping("/web/calendars/share")
public class CalendarSharingViewController implements HasLaunchpad {

    private static final String REDIRECT_WEB_CALENDARS_SHARE_PERSONS = "redirect:/web/calendars/share/persons/%d";

    private final PersonCalendarService personCalendarService;
    private final DepartmentCalendarService departmentCalendarService;
    private final CompanyCalendarService companyCalendarService;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final CalendarAccessibleService calendarAccessibleService;

    @Autowired
    CalendarSharingViewController(
        PersonCalendarService personCalendarService,
        DepartmentCalendarService departmentCalendarService,
        CompanyCalendarService companyCalendarService,
        PersonService personService,
        DepartmentService departmentService,
        CalendarAccessibleService calendarAccessibleService
    ) {
        this.personCalendarService = personCalendarService;
        this.departmentCalendarService = departmentCalendarService;
        this.companyCalendarService = companyCalendarService;
        this.personService = personService;
        this.departmentService = departmentService;
        this.calendarAccessibleService = calendarAccessibleService;
    }

    @GetMapping
    public String redirect() {
        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personService.getSignedInUser().getId());
    }

    @GetMapping("/persons/{personId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String index(@PathVariable long personId, Model model) {

        final PersonCalendarDto dto = getPersonCalendarDto(personId);
        model.addAttribute("privateCalendarShare", dto);

        final List<DepartmentCalendarDto> departmentCalendarDtos = getDepartmentCalendarDtos(personId);
        model.addAttribute("departmentCalendars", departmentCalendarDtos);
        model.addAttribute("calendarPeriods", CalendarPeriodViewType.values());

        final Person signedInUser = personService.getSignedInUser();
        prepareModelForOtherCalendarSharePerson(personId, model, signedInUser);
        prepareModelForCompanyCalendar(model, personId, signedInUser);

        return "calendarsharing/index";
    }

    @GetMapping("/persons/{personId}/departments/{activeDepartmentId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String indexDepartment(@PathVariable long personId, @PathVariable long activeDepartmentId, Model model) {

        final PersonCalendarDto dto = getPersonCalendarDto(personId);
        model.addAttribute("privateCalendarShare", dto);

        final List<DepartmentCalendarDto> departmentCalendarDtos = getDepartmentCalendarDtos(personId, activeDepartmentId);
        model.addAttribute("departmentCalendars", departmentCalendarDtos);
        model.addAttribute("calendarPeriods", CalendarPeriodViewType.values());

        final Person signedInUser = personService.getSignedInUser();
        prepareModelForOtherCalendarSharePerson(personId, model, signedInUser);
        prepareModelForCompanyCalendar(model, personId, signedInUser);

        return "calendarsharing/index";
    }

    private void prepareModelForOtherCalendarSharePerson(long personId, Model model, Person signedInUser) {
        final boolean isSignedInUser = personId == signedInUser.getId();
        model.addAttribute("isSignedInUser", isSignedInUser);

        if (!isSignedInUser) {
            final Optional<Person> calendarSharePerson = personService.getPersonByID(personId);
            model.addAttribute("personName", calendarSharePerson.map(Person::getNiceName).orElse(""));
        }
    }

    @PostMapping(value = "/persons/{personId}/me")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkPrivateCalendar(@PathVariable long personId, @Valid @ModelAttribute PersonCalendarDto personCalendarDto) {

        final Period calendarPeriod = personCalendarDto.getCalendarPeriod().getPeriod();
        personCalendarService.createCalendarForPerson(personId, calendarPeriod);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personId);
    }

    @PostMapping(value = "/persons/{personId}/me", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkPrivateCalendar(@PathVariable long personId) {

        personCalendarService.deletePersonalCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personId);
    }

    @PostMapping(value = "/persons/{personId}/departments/{departmentId}")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkDepartmentCalendar(@PathVariable long personId, @PathVariable int departmentId,
                                         @Valid @ModelAttribute DepartmentCalendarDto departmentCalendarDto) {

        final Period calendarPeriod = departmentCalendarDto.getCalendarPeriod().getPeriod();
        departmentCalendarService.createCalendarForDepartmentAndPerson(departmentId, personId, calendarPeriod);

        return format("redirect:/web/calendars/share/persons/%d/departments/%d", personId, departmentId);
    }

    @PostMapping(value = "persons/{personId}/departments/{departmentId}", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkDepartmentCalendar(@PathVariable long personId, @PathVariable int departmentId) {

        departmentCalendarService.deleteCalendarForDepartmentAndPerson(departmentId, personId);

        return format("redirect:/web/calendars/share/persons/%d/departments/%d", personId, departmentId);
    }

    @PostMapping(value = "/persons/{personId}/company")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String linkCompanyCalendar(@PathVariable long personId, @Valid @ModelAttribute CompanyCalendarDto companyCalendarDto) {

        final Period calendarPeriod = companyCalendarDto.getCalendarPeriod().getPeriod();
        companyCalendarService.createCalendarForPerson(personId, calendarPeriod);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personId);
    }

    @PostMapping(value = "/persons/{personId}/company", params = "unlink")
    @PreAuthorize(IS_BOSS_OR_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public String unlinkCompanyCalendar(@PathVariable long personId) {

        companyCalendarService.deleteCalendarForPerson(personId);

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personId);
    }

    @PostMapping(value = "/persons/{personId}/company/accessible")
    @PreAuthorize(IS_BOSS_OR_OFFICE)
    public String editCompanyCalendarAccessible(@PathVariable long personId, CompanyCalendarAccessibleDto companyCalendarAccessibleDto) {

        if (companyCalendarAccessibleDto.isAccessible()) {
            calendarAccessibleService.enableCompanyCalendar();
        } else {
            calendarAccessibleService.disableCompanyCalendar();
        }

        return format(REDIRECT_WEB_CALENDARS_SHARE_PERSONS, personId);
    }

    private void prepareModelForCompanyCalendar(Model model, long personId, Person signedInUser) {

        final boolean isBossOrOffice = signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS);
        final boolean companyCalendarAccessible = calendarAccessibleService.isCompanyCalendarAccessible();

        if (isBossOrOffice) {
            // feature: enable / disable companyCalendar
            final CompanyCalendarAccessibleDto companyCalendarAccessibleDto = new CompanyCalendarAccessibleDto();
            companyCalendarAccessibleDto.setAccessible(companyCalendarAccessible);
            model.addAttribute("companyCalendarAccessible", companyCalendarAccessibleDto);
        }

        if (isBossOrOffice || companyCalendarAccessible) {
            // feature: create / delete link for companyCalendar
            final CompanyCalendarDto companyCalendarDto = getCompanyCalendarDto(personId);
            model.addAttribute("companyCalendarShare", companyCalendarDto);
        }
    }

    private PersonCalendarDto getPersonCalendarDto(long personId) {
        final PersonCalendarDto dto = new PersonCalendarDto();
        dto.setPersonId(personId);

        final Optional<PersonCalendar> maybePersonCalendar = personCalendarService.getPersonCalendar(personId);
        if (maybePersonCalendar.isPresent()) {
            final PersonCalendar personCalendar = maybePersonCalendar.get();
            final var path = format("web/persons/%d/calendar?secret=%s", personId, personCalendar.getSecret());
            final var url = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath(path).build().toString();

            dto.setCalendarUrl(url);
            dto.setCalendarPeriod(CalendarPeriodViewType.ofPeriod(personCalendar.getCalendarPeriod()));
        }

        return dto;
    }

    private List<DepartmentCalendarDto> getDepartmentCalendarDtos(long personId) {
        return getDepartmentCalendarDtos(personId, null);
    }

    private List<DepartmentCalendarDto> getDepartmentCalendarDtos(long personId, @Nullable Long activeDepartmentId) {

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
                final var path = format("web/departments/%s/persons/%s/calendar?secret=%s", departmentId, personId, departmentCalendar.getSecret());
                final var url = ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .replacePath(path).build().toString();

                departmentCalendarDto.setCalendarUrl(url);
                departmentCalendarDto.setCalendarPeriod(CalendarPeriodViewType.ofPeriod(departmentCalendar.getCalendarPeriod()));
            }

            departmentCalendarDtos.add(departmentCalendarDto);
        }

        if (!departmentCalendarDtos.isEmpty() && departmentCalendarDtos.stream().noneMatch(DepartmentCalendarDto::isActive)) {
            departmentCalendarDtos.getFirst().setActive(true);
        }

        return departmentCalendarDtos;
    }

    private CompanyCalendarDto getCompanyCalendarDto(long personId) {

        final CompanyCalendarDto companyCalendarDto = new CompanyCalendarDto();
        companyCalendarDto.setPersonId(personId);

        final Optional<CompanyCalendar> maybeCompanyCalendar = companyCalendarService.getCompanyCalendar(personId);
        if (maybeCompanyCalendar.isPresent()) {

            final CompanyCalendar companyCalendar = maybeCompanyCalendar.get();
            final var path = format("web/company/persons/%d/calendar?secret=%s", personId, companyCalendar.getSecret());
            final var url = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath(path).build().toString();

            companyCalendarDto.setCalendarUrl(url);
            companyCalendarDto.setCalendarPeriod(CalendarPeriodViewType.ofPeriod(companyCalendar.getCalendarPeriod()));
        }

        return companyCalendarDto;
    }

    private Person getPersonOrThrow(long personId) {

        final Optional<Person> maybePerson = personService.getPersonByID(personId);
        if (maybePerson.isEmpty()) {
            throw new IllegalArgumentException("could not find person for given personId=" + personId);
        }

        return maybePerson.get();
    }
}
