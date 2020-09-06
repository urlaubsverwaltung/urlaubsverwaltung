package org.synyx.urlaubsverwaltung.overview.calendar;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_FIRST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.api.SwaggerConfig.EXAMPLE_LAST_DAY_OF_YEAR;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@RestControllerAdviceMarker
@Api("Vacations: Get all vacations for a certain period")
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

    @ApiOperation(
        value = "Get all allowed vacations for a person and a certain period of time",
        notes = "Get all allowed vacations for a person and a certain period of time. "
            + "Information only reachable for users with role office and for your own data."
    )
    @GetMapping
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public VacationsDto getVacations(
        @ApiParam(value = "ID of the person")
        @PathVariable("id")
            Integer personId,
        @ApiParam(value = "end of interval to get vacations from (inclusive)", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "end of interval to get vacations from (inclusive)", defaultValue = EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Person person = getPerson(personId);
        final List<Application> applications =
            applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate, endDate, person, ALLOWED);

        return mapToVacationResponse(applications);
    }

    @ApiOperation(
        value = "Get all allowed vacations for department members for the given person and the certain period",
        notes = "Get all allowed vacations for department members for the given person and the certain period. "
            + "All the waiting and allowed vacations of the departments the person is assigned to, are fetched. "
            + "Information only reachable for users with role office and for your own data."
    )
    @GetMapping(params = "ofDepartmentMembers")
    @PreAuthorize(IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public VacationsDto getVacationsOfOthersOrDepartmentColleagues(
        @ApiParam(value = "ID of the person")
        @PathVariable("id")
            Integer personId,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,
        @ApiParam(value = "If defined returns only the vacations of the department members of the person")
        @RequestParam(value = "ofDepartmentMembers", defaultValue = "true")
            boolean ofDepartmentMembers) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Parameter 'from' must be before or equals to 'to' parameter");
        }

        final Person person = getPerson(personId);

        final List<Application> applications;
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        if (departments.isEmpty()) {
            applications = applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate, ALLOWED);
        } else {
            applications = departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, startDate, endDate);
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
