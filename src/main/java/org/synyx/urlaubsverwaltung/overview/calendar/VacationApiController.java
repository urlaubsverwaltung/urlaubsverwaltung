package org.synyx.urlaubsverwaltung.overview.calendar;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCEL_RE;

@RestControllerAdviceMarker
@Api("Vacations: Get all vacations for a certain period")
@RestController("restApiVacationController")
@RequestMapping("/api")
public class VacationApiController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final DepartmentService departmentService;

    @Autowired
    VacationApiController(PersonService personService, ApplicationService applicationService,
                          DepartmentService departmentService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.departmentService = departmentService;
    }

    @ApiOperation(
        value = "Get all allowed vacations for a certain period",
        notes = "Get all allowed vacations for a certain period. "
            + "If a person is specified, only the allowed vacations of the person are fetched. "
            + "If a person and the department members flag is specified, "
            + "then all the waiting and allowed vacations of the departments the person is assigned to, are fetched. "
            + "Information only reachable for users with role office."
    )
    @GetMapping("/vacations")
    @PreAuthorize(SecurityRules.IS_OFFICE + " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)")
    public ResponseWrapper<VacationListResponse> vacations(
        @ApiParam(value = "Get vacations for department members of person")
        @RequestParam(value = "departmentMembers", required = false)
            Boolean departmentMembers,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam(value = "from")
            String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam(value = "to")
            String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
            Integer personId) {

        final LocalDate startDate;
        final LocalDate endDate;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);
            startDate = LocalDate.parse(from, fmt);
            endDate = LocalDate.parse(to, fmt);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        List<Application> applications = new ArrayList<>();

        if (personId == null && departmentMembers == null) {
            applications.addAll(applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate, ALLOWED));
            applications.addAll(applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate, ALLOWED_CANCEL_RE));
        }

        if (personId != null) {
            final Optional<Person> person = personService.getPersonByID(personId);

            if (person.isPresent()) {
                if (departmentMembers == null || !departmentMembers) {
                    applications.addAll(applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate, endDate, person.get(), ALLOWED));
                    applications.addAll(applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate, endDate, person.get(), ALLOWED_CANCEL_RE));
                } else {
                    applications = departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person.get(), startDate, endDate);
                }
            }
        }

        List<VacationResponse> vacationResponses = applications.stream().map(VacationResponse::new).collect(toList());

        return new ResponseWrapper<>(new VacationListResponse(vacationResponses));
    }
}
