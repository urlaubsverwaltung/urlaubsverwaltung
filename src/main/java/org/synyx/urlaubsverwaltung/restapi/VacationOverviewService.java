package org.synyx.urlaubsverwaltung.restapi;

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
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.ArrayList;
import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "VacationOverview", description = "Get Vacation-Overview Metadata")
@RestController("restApiVacationOverview")
@RequestMapping("/api")
public class VacationOverviewService {

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;
    private SessionService sessionService;

    @Autowired
    VacationOverviewService(DepartmentService departmentService, WorkingTimeService workingTimeService,
        PublicHolidaysService publicHolidayService, SessionService sessionService) {

        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
        this.sessionService = sessionService;
    }

    @ApiOperation(value = "Get Vacation-Overview Metadata", notes = "Get Vacation-Overview metadata for all members of a department")
    @RequestMapping(value = "/vacationoverview", method = RequestMethod.GET)
    public ResponseWrapper<VacationOverviewResponse> getHolydayOverview(
        @RequestParam("selectedDepartment") String selectedDepartment,
        @RequestParam("selectedYear") Integer selectedYear, @RequestParam("selectedMonth") Integer selectedMonth) {

        Department department = null;

        List<VacationOverview> holydayOverviewList = new ArrayList<VacationOverview>();
        for (Department item : departmentService.getAllDepartments()) {
            if (item.getName().equals(selectedDepartment)) {
                department = item;
                break;
            }
        }
        if (department != null) {

            List<Person> members = department.getMembers();
            for (Person person2 : members) {
                DateMidnight date = new DateMidnight();
                int year = selectedYear != null ? selectedYear : date.getYear();
                int month = selectedMonth != null ? selectedMonth : date.getMonthOfYear();
                DateMidnight firstDay = DateUtil.getFirstDayOfMonth(year, month);
                DateMidnight lastDay = DateUtil.getLastDayOfMonth(year, month);
                VacationOverview holydayOverview = new VacationOverview();
                holydayOverview.setDays(new ArrayList<DayOfMonth>());
                holydayOverview.setPerson(new PersonResponse(person2));
                holydayOverview.setPersonID(person2.getId());
                for (int i = 1; i <= lastDay.toDate().getDate(); i++) {
                    DayOfMonth dayOfMonth = new DayOfMonth();
                    DateMidnight currentDay = new DateMidnight(year, month, i);
                    dayOfMonth.setDayText(currentDay.toString("yyyy-MM-dd"));
                    dayOfMonth.setDayNumber(i);
                    FederalState state = workingTimeService.getFederalStateForPerson(person2, currentDay);
                    if (DateUtil.isWorkDay(currentDay)
                        && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {
                        dayOfMonth.setTypeOfDay(TypeOfDay.WORKDAY);
                    } else {
                        dayOfMonth.setTypeOfDay(TypeOfDay.WEEKEND);

                    }
                    holydayOverview.getDays().add(dayOfMonth);
                }
                holydayOverviewList.add(holydayOverview);
            }
        }

        return new ResponseWrapper<>(new VacationOverviewResponse(holydayOverviewList));
    }
}
