package org.synyx.urlaubsverwaltung.application.me;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.AccessDeniedException;
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
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.comparing;
import static org.springframework.util.StringUtils.hasText;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToCancelDirectlyApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToEditApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToRevokeApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationForLeavePermissionEvaluator.isAllowedToStartCancellationRequest;

@Controller
@RequestMapping("/")
public class ApplicationsViewController implements HasLaunchpad, HasPersonSearch {

    private static final String PERSON_ATTRIBUTE = "person";
    public static final String MY_APPLICATIONS_ANONYMOUS_PATH = "/web/persons/me/applications";
    public static final String MY_APPLICATIONS_PATH = "/web/persons/{personId}/applications";

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final VacationTypeViewModelService vacationTypeViewModelService;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    ApplicationsViewController(
        PersonService personService,
        DepartmentService departmentService,
        ApplicationService applicationService,
        WorkDaysCountService workDaysCountService,
        VacationTypeViewModelService vacationTypeViewModelService,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.vacationTypeViewModelService = vacationTypeViewModelService;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return (suggestion, context) -> {
            String url = "/web/persons/%s/applications".formatted(suggestion.getId());
            final String year = context.getRequest().getParameter("year");
            if (year != null) {
                url += "?year=" + year;
            }
            return url;
        };
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @GetMapping(MY_APPLICATIONS_ANONYMOUS_PATH)
    public String showMyApplications(@RequestParam(value = "year", required = false) String year) {
        final Person signedInUser = personService.getSignedInUser();
        if (hasText(year)) {
            return "redirect:/web/persons/" + signedInUser.getId() + "/applications?year=" + year;
        }

        return "redirect:/web/persons/" + signedInUser.getId() + "/applications";
    }


    @GetMapping(MY_APPLICATIONS_PATH)
    public String showMyApplications(
        @PathVariable("personId") Long personId,
        @RequestParam(value = "year", required = false) Integer year, Model model, Locale locale
    )
        throws UnknownPersonException {

        final Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        final Person signedInUser = personService.getSignedInUser();

        if (!departmentService.isSignedInUserAllowedToAccessPersonData(signedInUser, person)) {
            throw new AccessDeniedException(
                "User '%s' has not the correct permissions to access the applications of user '%s'".formatted(signedInUser.getId(), person.getId()));
        }

        model.addAttribute(PERSON_ATTRIBUTE, person);
        model.addAttribute("departmentsOfPerson", departmentService.getAssignedDepartmentsOfMember(person));

        final LocalDate now = LocalDate.now(clock);
        final int yearToShow = year == null ? now.getYear() : year;

        final List<VacationTypeDto> vacationTypeColors = vacationTypeViewModelService.getVacationTypeColors();
        model.addAttribute("vacationTypeColors", vacationTypeColors);

        prepareApplications(person, signedInUser, yearToShow, model, locale);

        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("selectedYear", yearToShow);
        model.addAttribute("signedInUser", signedInUser);

        return "me/applications";
    }

    private void prepareApplications(Person person, Person signedInUser, int year, Model model, Locale locale) {

        // get the person's applications for the given year
        final LocalDate startDate = Year.of(year).atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());
        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        final List<ApplicationDto> applicationsForLeave;
        final YearlyUsedDaysSummary usedDaysOverview;

        if (applications.isEmpty()) {
            applicationsForLeave = List.of();
            usedDaysOverview = new YearlyUsedDaysSummary(List.of(), year, workDaysCountService);
        } else {
            applicationsForLeave = applications.stream()
                .map(application -> new ApplicationForLeave(application, workDaysCountService))
                .sorted(comparing(ApplicationForLeave::getStartDate).reversed())
                .map(applicationForLeave -> applicationDto(applicationForLeave, signedInUser, locale))
                .toList();
            usedDaysOverview = new YearlyUsedDaysSummary(applications, year, workDaysCountService);
        }

        model.addAttribute("applications", applicationsForLeave);
        model.addAttribute("usedDaysOverview", usedDaysOverview);
    }

    private ApplicationVacationTypeDto applicationVacationTypDto(VacationType<?> vacationType, Locale locale) {
        return new ApplicationVacationTypeDto(vacationType.getLabel(locale), vacationType.getCategory(), vacationType.getColor());
    }

    private ApplicationDto applicationDto(ApplicationForLeave applicationForLeave, Person signedInUser, Locale locale) {

        final List<PersonDto> holidayReplacements = applicationForLeave.getHolidayReplacements().stream()
            .map(hr -> new PersonDto(hr.getPerson().getGravatarURL(), hr.getPerson().getNiceName(), hr.getPerson().getInitials()))
            .toList();

        final boolean requiresApprovalToCancel = applicationForLeave.getVacationType().isRequiresApprovalToCancel();
        final boolean isDepartmentHeadOfPerson = departmentService.isDepartmentHeadAllowedToManagePerson(signedInUser, applicationForLeave.getPerson());
        final boolean isSecondStageAuthorityOfPerson = departmentService.isSecondStageAuthorityAllowedToManagePerson(signedInUser, applicationForLeave.getPerson());

        final boolean allowedToEdit = isAllowedToEditApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson);

        final boolean allowedToRevoke = isAllowedToRevokeApplication(applicationForLeave, signedInUser, requiresApprovalToCancel);
        final boolean allowedToCancel = isAllowedToCancelApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson);
        final boolean allowedToCancelDirectly = isAllowedToCancelDirectlyApplication(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel);
        final boolean allowedToStartCancellationRequest = isAllowedToStartCancellationRequest(applicationForLeave, signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson, requiresApprovalToCancel);

        final ApplicationDto dto = new ApplicationDto();
        dto.setId(applicationForLeave.getId());
        dto.setStatus(applicationForLeave.getStatus());
        dto.setVacationType(applicationVacationTypDto(applicationForLeave.getVacationType(), locale));
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
        dto.setHolidayReplacements(holidayReplacements);
        dto.setAllowedToEdit(allowedToEdit);
        dto.setAllowedToRevoke(allowedToRevoke);
        dto.setAllowedToCancel(allowedToCancel);
        dto.setAllowedToCancelDirectly(allowedToCancelDirectly);
        dto.setAllowedToStartCancellationRequest(allowedToStartCancellationRequest);
        return dto;
    }
}
