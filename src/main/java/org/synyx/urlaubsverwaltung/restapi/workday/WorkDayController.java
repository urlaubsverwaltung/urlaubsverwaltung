package org.synyx.urlaubsverwaltung.restapi.workday;

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

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Work Days", description = "Get information about work day in a certain period")
@RestController("restApiWorkDayController")
@RequestMapping("/api")
public class WorkDayController {

    private final PersonService personService;
    private final WorkDaysService workDaysService;

    @Autowired
    WorkDayController(PersonService personService, WorkDaysService workDaysService) {

        this.personService = personService;
        this.workDaysService = workDaysService;
    }

    /**
     * Calculate number of work days for the given period and person.
     *
     * @param  from  start date as String (e.g. 2013-3-21)
     * @param  to  end date as String (e.g. 2013-3-21)
     * @param  length  day length as String (FULL, MORNING or NOON)
     * @param  personId  id of the person to number of work days for
     *
     * @return  number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @ApiOperation(
        value = "Calculate the work days for a certain period and person",
        notes = "The calculation depends on the working time of the person."
    )
    @RequestMapping(value = "/workdays", method = RequestMethod.GET)
    public ResponseWrapper<WorkDayResponse> workDays(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2016-01-01")
        @RequestParam("from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2016-01-08")
        @RequestParam("to")
        String to,
        @ApiParam(value = "Day Length", defaultValue = "FULL", allowableValues = "FULL, MORNING, NOON")
        @RequestParam("length")
        String length,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId) {

        DateTimeFormatter fmt = DateTimeFormat.forPattern(RestApiDateFormat.DATE_PATTERN);
        DateMidnight startDate = DateMidnight.parse(from, fmt);
        DateMidnight endDate = DateMidnight.parse(to, fmt);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        Optional<Person> person = personService.getPersonByID(personId);

        if (!person.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        DayLength howLong = DayLength.valueOf(length);
        BigDecimal days = workDaysService.getWorkDays(howLong, startDate, endDate, person.get());

        return new ResponseWrapper<>(new WorkDayResponse(days.toString()));
    }
}
