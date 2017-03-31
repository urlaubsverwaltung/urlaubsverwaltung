package org.synyx.urlaubsverwaltung.restapi;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.ColoredDay;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
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
			@RequestParam("selectedDepartment")
			String selectedDepartment,
			@RequestParam("selectedYear")
			Integer selectedYear,
			@RequestParam("selectedMonth")
			Integer selectedMonth) {

		Department department = null;
		Person user = sessionService.getSignedInUser();
		
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
				holydayOverview.setDays(new ArrayList<ColoredDay>());
				holydayOverview.setPerson(new PersonResponse(person2));
				holydayOverview.setPersonID(person2.getId());
				for (int i = 1; i <= lastDay.toDate().getDate(); i++) {
					ColoredDay coloredDay = new ColoredDay();
					DateMidnight currentDay = new DateMidnight(year, month, i);
					coloredDay.setDay(currentDay.toString("yyyy-MM-dd"));
					coloredDay.setIntValue(i);
					FederalState state = workingTimeService.getFederalStateForPerson(person2, currentDay);
					if (DateUtil.isWorkDay(currentDay)
							&& (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {
						coloredDay.setColorCode("#FFFFFF;");
					} else {
						coloredDay.setColorCode("#DCDCDC;");

					}
					holydayOverview.getDays().add(coloredDay);
				}
				holydayOverviewList.add(holydayOverview);
			}

			DateMidnight date = new DateMidnight();
			DateMidnight firstDay = DateUtil.getFirstDayOfMonth(date.getYear(), date.getMonthOfYear());
			DateMidnight lastDay = DateUtil.getLastDayOfMonth(date.getYear(), date.getMonthOfYear());
			List<ColoredDay> headerDayList = new ArrayList<ColoredDay>();
			for (int i = 1; i <= lastDay.toDate().getDate(); i++) {
				ColoredDay coloredDay = new ColoredDay();
				coloredDay.setIntValue(i);
				DateMidnight d = new DateMidnight(lastDay.getYear(), lastDay.getMonthOfYear(), i);
				FederalState state = workingTimeService.getFederalStateForPerson(user, d);
				if (DateUtil.isWorkDay(d)
						&& (publicHolidayService.getWorkingDurationOfDate(d, state).longValue() > 0)) {
					coloredDay.setColorCode("#FFFFFF;");
				} else {
					coloredDay.setColorCode("#DCDCDC;");
				}
				headerDayList.add(coloredDay);
			}
		}

		return new ResponseWrapper<>(new VacationOverviewResponse(holydayOverviewList));
	}
}
