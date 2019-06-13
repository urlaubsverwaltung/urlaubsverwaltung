package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.absence.api.AbsenceController;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.util.List;

@Api("VacationOverview: Get Vacation-Overview Metadata")
@RestController("restApiVacationOverview")
@RequestMapping("/api")
public class VacationOverviewController {

    private final VacationOverviewService vacationOverviewService;

    private final AbsenceController absenceController;

    @Autowired
    VacationOverviewController(VacationOverviewService vacationOverviewService, PersonService personService, ApplicationService applicationService, SickNoteService sickNoteService) {
        this.vacationOverviewService = vacationOverviewService;
        this.absenceController = new AbsenceController(personService, applicationService,  sickNoteService);
    }

    @ApiOperation(
            value = "Get Vacation-Overview Metadata",
            notes = "Get Vacation-Overview metadata for all members of a department")
    @GetMapping("/vacationoverview")
    public ResponseWrapper<VacationOverviewResponse> getHolidayOverview(
            @RequestParam("selectedDepartment") String selectedDepartment,
            @RequestParam("selectedYear") Integer selectedYear,
            @RequestParam("selectedMonth") Integer selectedMonth) {

        List<VacationOverview> holidayOverviewList =
                vacationOverviewService.getVacationOverviews(selectedDepartment, selectedYear, selectedMonth);

        // to provide an efficient facade, also pull in the absences that would otherwise require separate API calls
        for (VacationOverview v: holidayOverviewList){
            v.setAbsences(
                absenceController.personsVacations(selectedYear, selectedMonth, v.getPersonID(), null).getResponse().getAbsences()
            );
        }

        return new ResponseWrapper<>(new VacationOverviewResponse(holidayOverviewList));
    }
}
