package org.synyx.urlaubsverwaltung.absence.web.me;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeDto;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeViewModelService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.comparing;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.person.Role.APPLICATION_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Controller
@RequestMapping("/")
public class MeAbsencesViewController implements HasLaunchpad {

    private static final String PERSON_ATTRIBUTE = "person";

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final Clock clock;

    public MeAbsencesViewController(PersonService personService, ApplicationService applicationService, WorkDaysCountService workDaysCountService, DepartmentService departmentService, VacationTypeViewModelService vacationTypeViewModelService, Clock clock) {
        this.personService = personService;
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.clock = clock;
    }

    @GetMapping("/web/my-absences")
    public String showOverview(@RequestParam(value = "year", required = false) String year) {
        final Person signedInUser = personService.getSignedInUser();
        if (hasText(year)) {
            return "redirect:/web/person/" + signedInUser.getId() + "/absences?year=" + year;
        }

        return "redirect:/web/person/" + signedInUser.getId() + "/absences";
    }


    @GetMapping("/web/person/{personId}/absences")
    public String showAbsences(@PathVariable("personId") Long personId,
                               @RequestParam(value = "year", required = false) Integer year, Model model, Locale locale)
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        model.addAttribute(PERSON_ATTRIBUTE, person);

        final LocalDate now = LocalDate.now(clock);
        final int yearToShow = year == null ? now.getYear() : year;

        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);

        prepareApplications(person, yearToShow, model, locale);

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("selectedYear", yearToShow);
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("signedInUser", signedInUser);

        model.addAttribute("canAccessAbsenceOverview", person.equals(signedInUser));
        model.addAttribute("canAccessCalendarShare", person.equals(signedInUser) || signedInUser.hasRole(OFFICE) || signedInUser.hasRole(BOSS));
        model.addAttribute("canAddApplicationForLeaveForAnotherUser", signedInUser.hasRole(OFFICE) || isPersonAllowedToExecuteRoleOn(signedInUser, APPLICATION_ADD, person));
        return "me/absence/absence";
    }

    private void prepareApplications(Person person, int year, Model model, Locale locale) {

        // get the person's applications for the given year
        final LocalDate startDate = Year.of(year).atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());
        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        final List<MeApplicationDto> applicationsForLeave;
        final MeUsedDaysDto usedDaysOverview;

        if (applications.isEmpty()) {
            applicationsForLeave = List.of();
            usedDaysOverview = new MeUsedDaysDto(List.of(), year, workDaysCountService);
        } else {
            applicationsForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, workDaysCountService))
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .map(applicationForLeave -> applicationDto(applicationForLeave, locale))
                .toList();
            usedDaysOverview = new MeUsedDaysDto(applications, year, workDaysCountService);
        }

        model.addAttribute("applications", applicationsForLeave);
        model.addAttribute("usedDaysOverview", usedDaysOverview);
    }

    private MeVacationTypDto overviewVacationTypDto(VacationType<?> vacationType, Locale locale) {
        return new MeVacationTypDto(vacationType.getLabel(locale), vacationType.getCategory(), vacationType.getColor());
    }

    private MeApplicationDto applicationDto(ApplicationForLeave applicationForLeave, Locale locale) {
        final MeApplicationDto dto = new MeApplicationDto();
        dto.setId(applicationForLeave.getId());
        dto.setStatus(applicationForLeave.getStatus());
        dto.setVacationType(overviewVacationTypDto(applicationForLeave.getVacationType(), locale));
        dto.setApplicationDate(applicationForLeave.getApplicationDate());
        dto.setStartDate(applicationForLeave.getStartDate());
        dto.setEndDate(applicationForLeave.getEndDate());
        dto.setStartTime(applicationForLeave.getStartTime());
        dto.setEndTime(applicationForLeave.getEndTime());
        dto.setStartDateWithTime(applicationForLeave.getStartDateWithTime());
        dto.setEndDateWithTime(applicationForLeave.getEndDateWithTime());
        dto.setDayLength(applicationForLeave.getDayLength());
        dto.setWorkDays(applicationForLeave.getWorkDays());
        dto.setPersonId(applicationForLeave.getPerson().getId());
        dto.setHours(applicationForLeave.getHours());
        dto.setWeekDayOfStartDate(applicationForLeave.getWeekDayOfStartDate());
        dto.setWeekDayOfEndDate(applicationForLeave.getWeekDayOfEndDate());
        dto.setEditedDate(applicationForLeave.getEditedDate());
        dto.setCancelDate(applicationForLeave.getCancelDate());
        return dto;
    }

    private boolean isPersonAllowedToExecuteRoleOn(Person person, Role role, Person personToShowDetails) {
        final boolean isBossOrDepartmentHeadOrSecondStageAuthority = person.hasRole(BOSS)
            || departmentService.isDepartmentHeadAllowedToManagePerson(person, personToShowDetails)
            || departmentService.isSecondStageAuthorityAllowedToManagePerson(person, personToShowDetails);
        return person.hasRole(role) && isBossOrDepartmentHeadOrSecondStageAuthority;
    }
}
