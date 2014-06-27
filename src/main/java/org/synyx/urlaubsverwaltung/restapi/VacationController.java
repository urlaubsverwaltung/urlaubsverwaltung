package org.synyx.urlaubsverwaltung.restapi;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.google.gson.Gson;

import com.wordnik.swagger.annotations.Api;

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
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Vacations", description = "Get all vacations for a certain period")
@Controller("restApiVacationController")
public class VacationController {

    private static final String ROOT_URL = "/vacation";

    @Autowired
    private PersonService personService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OwnCalendarService ownCalendarService;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public VacationListResponse vacations(@RequestParam(value = "from", required = true) String from,
        @RequestParam(value = "to", required = true) String to) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        List<Application> applications = applicationService.getAllowedApplicationsForACertainPeriod(startDate, endDate);

        List<AbsenceResponse> vacationResponses = Lists.transform(applications,
                new Function<Application, AbsenceResponse>() {

                    @Override
                    public AbsenceResponse apply(Application application) {

                        return new AbsenceResponse(application);
                    }
                });

        return new VacationListResponse(vacationResponses);
    }


    /**
     * Calculate number of vacation days for given period and person.
     *
     * @param  start  start date as String (e.g. 2013-3-21)
     * @param  end  end date as String (e.g. 2013-3-21)
     * @param  length  day length as String (FULL, MORNING or NOON)
     * @param  personId  id of the person to calculate used days for
     *
     * @return  number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @RequestMapping(value = ROOT_URL + "/calculate", method = RequestMethod.GET)
    @ResponseBody
    public String numberOfVacationDays(@RequestParam("start") String start,
        @RequestParam("end") String end,
        @RequestParam("length") String length,
        @RequestParam("person") Integer personId) {

        if (StringUtils.hasText(start) && StringUtils.hasText(end) && StringUtils.hasText(length)) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
            DateMidnight startDate = DateMidnight.parse(start, fmt);
            DateMidnight endDate = DateMidnight.parse(end, fmt);

            if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                DayLength howLong = DayLength.valueOf(length);
                Person person = personService.getPersonByID(personId);
                BigDecimal days = ownCalendarService.getWorkDays(howLong, startDate, endDate, person);

                return days.toString();
            }
        }

        return "N/A";
    }


    @RequestMapping(value = ROOT_URL + "/application-info", method = RequestMethod.GET)
    @ResponseBody
    public String personsVacations(@RequestParam("year") String year,
        @RequestParam(value = "month", required = false) String month,
        @RequestParam("person") Integer personId) {

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        if (hasYear && personId != null) {
            try {
                Person person = personService.getPersonByID(personId);

                if (person == null) {
                    return "N/A";
                }

                List<Application> applications = hasMonth
                    ? applicationService.getAllAllowedApplicationsOfAPersonForAMonth(person, Integer.parseInt(month),
                        Integer.parseInt(year))
                    : applicationService.getAllApplicationsByPersonAndYearAndState(person, Integer.parseInt(year),
                        ApplicationStatus.ALLOWED);

                List<VacationDate> vacationDateList = new ArrayList<>();

                for (Application app : applications) {
                    DateMidnight startDate = app.getStartDate();
                    DateMidnight endDate = app.getEndDate();

                    DateMidnight day = startDate;

                    while (!day.isAfter(endDate)) {
                        vacationDateList.add(new VacationDate(day.toString(RestApiDateFormat.PATTERN), app.getId(),
                                app.getStatus().name()));

                        day = day.plusDays(1);
                    }
                }

                String json = new Gson().toJson(vacationDateList);

                return json;
            } catch (NumberFormatException ex) {
                return "N/A";
            }
        }

        return "N/A";
    }

    private class VacationDate {

        private String date;
        private Integer applicationId;
        private String status;

        public VacationDate(String date, Integer applicationId, String status) {

            this.date = date;
            this.applicationId = applicationId;
            this.status = status;
        }

        @Override
        public String toString() {

            return "date = " + this.date + ", href = " + this.applicationId + ", status = " + status;
        }
    }
}
