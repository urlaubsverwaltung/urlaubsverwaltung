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
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Tag(name = "vacations", description = "Vacations: Get all vacations for a certain period")
@RestController
@RequestMapping("/api/persons/{id}/vacations")
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
        deprecated = true,
        summary = "Get all allowed vacations for a person and a certain period of time",
        description = "Get all allowed vacations for a person and a certain period of time. "
            + "Information only reachable for users with role office and for your own data."
    )
    @GetMapping
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public VacationsDto getVacations(
        @Parameter(description = "ID of the person")
        @PathVariable("id")
            Integer personId,
        @Parameter(description = "end of interval to get vacations from (inclusive)")
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @Parameter(description = "end of interval to get vacations from (inclusive)")
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Person person = getPerson(personId);
        final List<Application> applications = new ArrayList<>();
        applications.addAll(applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate, endDate, person, ALLOWED));
        applications.addAll(applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate, endDate, person, ALLOWED_CANCELLATION_REQUESTED));

        return mapToVacationResponse(applications);
    }

    @Operation(
        summary = "Get all allowed vacations for department members for the given person and the certain period",
        description = "Get all allowed vacations for department members for the given person and the certain period. "
            + "All the waiting and allowed vacations of the departments the person is assigned to, are fetched. "
            + "Information only reachable for users with role office and for your own data."
    )
    @GetMapping(params = "ofDepartmentMembers")
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public VacationsDto getVacationsOfOthersOrDepartmentColleagues(
        @Parameter(description = "ID of the person")
        @PathVariable("id")
            Integer personId,
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

        final Person person = getPerson(personId);

        final List<Application> applications = new ArrayList<>();
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        if (departments.isEmpty()) {
            applications.addAll(applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate, ALLOWED));
            applications.addAll(applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate, ALLOWED_CANCELLATION_REQUESTED));

        } else {
            applications.addAll(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, startDate, endDate));
        }

        return mapToVacationResponse(applications);
    }

    private Person getPerson(Integer personId) {
        return personService.getPersonByID(personId)
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No person found for id = " + personId));
    }

    private VacationsDto mapToVacationResponse(List<Application> applications) {
        final List<VacationDto> vacationsDto = applications.stream().map(VacationDto::new).collect(toList());
        return new VacationsDto(vacationsDto);
    }
}
