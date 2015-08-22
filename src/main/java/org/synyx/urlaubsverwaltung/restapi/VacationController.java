package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.collect.Lists;

import com.google.gson.Gson;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Vacations", description = "Get all vacations for a certain period")
@Controller("restApiVacationController")
public class VacationController {

    private static final String ROOT_URL = "/vacations";

    @Autowired
    private PersonService personService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(
        value = "Get all allowed vacations for a certain period",
        notes = "Get all allowed vacations for a certain period. "
            + "If a person is specified, only the allowed vacations of the person are fetched. "
            + "If a person and the department members flag is specified, "
            + "then all the waiting and allowed vacations of the departments the person is assigned to, are fetched."
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public VacationListResponse vacations(
        @ApiParam(value = "Get vacations for department members of person")
        @RequestParam(value = "departmentMembers", required = false)
        Boolean departmentMembers,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2015-01-01")
        @RequestParam(value = "from", required = true)
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2015-12-31")
        @RequestParam(value = "to", required = true)
        String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
        Integer personId) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

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

        List<AbsenceResponse> vacationResponses = Lists.transform(applications,
                application -> new AbsenceResponse(application));

        return new VacationListResponse(vacationResponses);
    }


    @ApiOperation(
        value = "Get all allowed vacation days for a certain period and person",
        notes = "Get all allowed vacation days for a certain period and person"
    )
    @RequestMapping(value = ROOT_URL + "/days", method = RequestMethod.GET)
    @ResponseBody
    public String personsVacations(
        @ApiParam(value = "Year to get the vacation days for", defaultValue = "2015")
        @RequestParam("year")
        String year,
        @ApiParam(value = "Month of year to get the vacation days for")
        @RequestParam(value = "month", required = false)
        String month,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId) {

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        if (hasYear && personId != null) {
            try {
                Optional<Person> person = personService.getPersonByID(personId);

                if (!person.isPresent()) {
                    return "N/A";
                }

                DateMidnight periodStart;
                DateMidnight periodEnd;

                if (hasMonth) {
                    periodStart = DateUtil.getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                    periodEnd = DateUtil.getLastDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                } else {
                    periodStart = DateUtil.getFirstDayOfYear(Integer.parseInt(year));
                    periodEnd = DateUtil.getLastDayOfYear(Integer.parseInt(year));
                }

                List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(
                            periodStart, periodEnd, person.get())
                    .stream()
                    .filter(application ->
                                application.hasStatus(ApplicationStatus.WAITING)
                                || application.hasStatus(ApplicationStatus.ALLOWED))
                    .collect(Collectors.toList());

                List<VacationDay> vacationDateList = new ArrayList<>();

                for (Application app : applications) {
                    DateMidnight startDate = app.getStartDate();
                    DateMidnight endDate = app.getEndDate();

                    DateMidnight day = startDate;

                    while (!day.isAfter(endDate)) {
                        vacationDateList.add(new VacationDay(day.toString(RestApiDateFormat.PATTERN), app.getId(),
                                app.getStatus().name(), app.getDayLength().getDuration()));

                        day = day.plusDays(1);
                    }
                }

                return new Gson().toJson(vacationDateList);
            } catch (NumberFormatException ex) {
                return "N/A";
            }
        }

        return "N/A";
    }

    private class VacationDay {

        private final String date;
        private final Integer applicationId;
        private final String status;
        private final BigDecimal dayLength;

        public VacationDay(String date, Integer applicationId, String status, BigDecimal dayLength) {

            this.date = date;
            this.applicationId = applicationId;
            this.status = status;
            this.dayLength = dayLength;
        }

        @Override
        public String toString() {

            return "date = " + this.date + ", href = " + this.applicationId + ", status = " + status + ", dayLength = "
                + dayLength;
        }
    }
}
