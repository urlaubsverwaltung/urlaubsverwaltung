package org.synyx.urlaubsverwaltung.absence.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.VacationOverviewService;

import java.util.List;
import java.util.Optional;


@Api("Absences: Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceController {

    private final PersonService personService;
    private final VacationOverviewService vacationOverviewService;

    @Autowired
    public AbsenceController(PersonService personService, VacationOverviewService vacationOverviewService) {

        this.personService = personService;
        this.vacationOverviewService = vacationOverviewService;
    }

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @GetMapping("/absences")
    public ResponseWrapper<DayAbsenceList> personsVacations(
        @ApiParam(value = "Year to get the absences for", defaultValue = RestApiDateFormat.EXAMPLE_YEAR)
        @RequestParam("year")
        Integer year,
        @ApiParam(value = "Month of year to get the absences for")
        @RequestParam(value = "month", required = false)
        Integer month,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId,
        @ApiParam(value = "Type of absences, vacation or sick notes", allowableValues = "VACATION, SICK_NOTE")
        @RequestParam(value = "type", required = false)
        String type) {


        Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        Optional<DayAbsence.Type> typeFilter =  type == null ? Optional.empty() : Optional.of(DayAbsence.Type.valueOf(type));

        List<DayAbsence> absences = vacationOverviewService.personsVacations(optionalPerson.get(), year, Optional.ofNullable(month), typeFilter);

        return new ResponseWrapper<>(new DayAbsenceList(absences));
    }

}
