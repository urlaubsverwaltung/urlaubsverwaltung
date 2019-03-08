package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;

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
