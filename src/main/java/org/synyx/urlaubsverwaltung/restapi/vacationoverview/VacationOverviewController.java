package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.ArrayList;
import java.util.List;

@Api(value = "VacationOverview", description = "Get Vacation-Overview Metadata")
@RestController("restApiVacationOverview")
@RequestMapping("/api")
public class VacationOverviewController {

    private VacationOverviewService vacationOverviewService;

    @Autowired
    VacationOverviewController(VacationOverviewService vacationOverviewService) {
        this.vacationOverviewService = vacationOverviewService;
    }

    @ApiOperation(
            value = "Get Vacation-Overview Metadata",
            notes = "Get Vacation-Overview metadata for all members of a department")
    @RequestMapping(value = "/vacationoverview", method = RequestMethod.GET)
    public ResponseWrapper<VacationOverviewResponse> getHolydayOverview(
            @RequestParam("selectedDepartment") String selectedDepartment,
            @RequestParam("selectedYear") Integer selectedYear,
            @RequestParam("selectedMonth") Integer selectedMonth) {

        List<VacationOverview> holidayOverviewList =
                vacationOverviewService.getVacationOverviews(selectedDepartment, selectedYear, selectedMonth);

        return new ResponseWrapper<>(new VacationOverviewResponse(holidayOverviewList));
    }
}
