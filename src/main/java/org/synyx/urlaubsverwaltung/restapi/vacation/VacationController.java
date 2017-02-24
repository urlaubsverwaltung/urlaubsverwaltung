package org.synyx.urlaubsverwaltung.restapi.vacation;

import com.google.common.collect.Lists;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Vacations", description = "Get all vacations for a certain period")
@RestController("restApiVacationController")
@RequestMapping("/api")
public class VacationController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final DepartmentService departmentService;

    @Autowired
    VacationController(PersonService personService, ApplicationService applicationService,
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
    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public ResponseWrapper<VacationListResponse> vacations(
        @ApiParam(value = "Get vacations for department members of person")
        @RequestParam(value = "departmentMembers", required = false)
        Boolean departmentMembers,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2016-01-01")
        @RequestParam(value = "from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2016-12-31")
        @RequestParam(value = "to")
        String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
        Integer personId) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.DATE_PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        List<Application> applications = new ArrayList<>();

        if (personId == null && departmentMembers == null) {
            applications = applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate,
                    ApplicationStatus.ALLOWED);
        }

        if (personId != null) {
            Optional<Person> person = personService.getPersonByID(personId);

            if (person.isPresent()) {
                if (departmentMembers == null || !departmentMembers) {
                    applications = applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate,
                            endDate, person.get(), ApplicationStatus.ALLOWED);
                } else {
                    applications = departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person.get(),
                            startDate, endDate);
                }
            }
        }

        List<AbsenceResponse> vacationResponses = Lists.transform(applications, AbsenceResponse::new);

        return new ResponseWrapper<>(new VacationListResponse(vacationResponses));
    }
}
