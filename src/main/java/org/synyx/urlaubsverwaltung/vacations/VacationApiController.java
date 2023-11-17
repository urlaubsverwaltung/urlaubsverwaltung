package org.synyx.urlaubsverwaltung.vacations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Tag(
    name = "vacations",
    description = """
        Vacations: Returns all vacations for a certain period
        """
)
@RestControllerAdviceMarker
@RestController
@RequestMapping("/api/persons/{personId}/vacations")
public class VacationApiController {

    public static final String VACATIONS = "vacations";

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final DepartmentService departmentService;

    @Autowired
    VacationApiController(PersonService personService, ApplicationService applicationService, DepartmentService departmentService) {
        this.personService = personService;
        this.applicationService = applicationService;
        this.departmentService = departmentService;
    }

    @Operation(
        summary = "Returns all active vacations for a person and a certain period of time",
        description = """
            Get all active vacations for a person and a certain period of time.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested vacations of the person id is the one of the authenticated user
            * department_head        - if the requested vacations of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested vacations of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested vacations of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public VacationsDto getVacations(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "end of interval to get vacations from (inclusive)")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "end of interval to get vacations from (inclusive)")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate,
        @Parameter(description = "List of the vacation status to return. Default are all active status - waiting, temporary_allowed, allowed, allowed_cancellation_requested")
        @RequestParam(value = "status", required = false, defaultValue = "waiting, temporary_allowed, allowed, allowed_cancellation_requested")
        List<String> applicationStatus) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final List<ApplicationStatus> requestedApplicationStatus = toApplicationStatus(applicationStatus);
        final List<Person> requestedPerson = List.of(getPerson(personId));

        final List<Application> applications = applicationService.getForStatesAndPerson(requestedApplicationStatus, requestedPerson, startDate, endDate);
        return mapToVacationResponse(applications);
    }

    @Operation(
        hidden = true,
        summary = "Get all active vacations for department members for the given person and the certain period",
        description = """
            Returns all active vacations for department members for the given person and the certain period.
            All active vacations of the departments the person is assigned to, are fetched.

            Needed basic authorities:
            * user

            Needed additional authorities:
            * user                   - if the requested vacations of the person id is the one of the authenticated user
            * department_head        - if the requested vacations of the person id is a managed person of the department head and not of the authenticated user
            * second_stage_authority - if the requested vacations of the person id is a managed person of the second stage authority and not of the authenticated user
            * boss or office         - if the requested vacations of the person id is any id but not of the authenticated user
            """
    )
    @GetMapping(params = "ofDepartmentMembers", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfSecondStageAuthority(authentication, #personId)")
    public VacationsDto getVacationsOfOthersOrDepartmentColleagues(
        @Parameter(description = "ID of the person")
        @PathVariable("personId")
        Long personId,
        @Parameter(description = "Start date with pattern yyyy-MM-dd")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "End date with pattern yyyy-MM-dd")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate endDate,
        @Parameter(description = "If defined returns only the vacations of the department members of the person")
        @RequestParam(value = "ofDepartmentMembers", defaultValue = "true")
        boolean ofDepartmentMembers) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Person requestedPerson = getPerson(personId);

        final List<Application> applicationsOfColleagues = departmentService.getApplicationsFromColleaguesOf(requestedPerson, startDate, endDate);
        return mapToVacationResponse(applicationsOfColleagues);
    }

    private Person getPerson(Long personId) {
        return personService.getPersonByID(personId)
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId));
    }

    private VacationsDto mapToVacationResponse(List<Application> applications) {
        return new VacationsDto(applications.stream().map(VacationDto::new).toList());
    }

    private List<ApplicationStatus> toApplicationStatus(List<String> applicationStatus) {
        if (applicationStatus.isEmpty()) {
            return List.of();
        }

        try {
            return applicationStatus.stream()
                .map(String::toUpperCase)
                .map(ApplicationStatus::valueOf)
                .toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }
}
