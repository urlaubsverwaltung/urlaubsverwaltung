package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.web.bind.annotation.RequestParam;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WORKDAY;


@Component
public class VacationOverviewService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;

    @Autowired
    VacationOverviewService(DepartmentService departmentService, WorkingTimeService workingTimeService,
        PublicHolidaysService publicHolidayService) {

        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
    }

    public List<VacationOverview> getVacationOverviews(String selectedDepartment, Integer selectedYear,
        Integer selectedMonth) {

        List<VacationOverview> holidayOverviewList = new ArrayList<>();

        Department department = getDepartmentByName(selectedDepartment);

        Calendar calendar = new GregorianCalendar();
        int year = selectedYear != null ? selectedYear : calendar.get(Calendar.YEAR);
        int month = selectedMonth != null ? selectedMonth - 1 : calendar.get(Calendar.MONTH);
        calendar.set(year, month, 1);

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (department != null) {
            for (Person person : department.getMembers()) {
                VacationOverview holidayOverview = getVacationOverview(person);

                for (int i = 1; i <= lastDay; i++) {
                    DayOfMonth dayOfMonth = new DayOfMonth();
                    DateMidnight currentDay = new DateMidnight(year, month + 1, i);

                    dayOfMonth.setDayText(currentDay.toString(DATE_FORMAT));
                    dayOfMonth.setDayNumber(i);
                    dayOfMonth.setTypeOfDay(getTypeOfDay(person, currentDay));

                    holidayOverview.getDays().add(dayOfMonth);
                }

                holidayOverviewList.add(holidayOverview);
            }
        }

        return holidayOverviewList;
    }


    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, DateMidnight currentDay) {

        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, currentDay);

        if (DateUtil.isWorkDay(currentDay)
                && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {
            typeOfDay = WORKDAY;
        } else {
            typeOfDay = WEEKEND;
        }

        return typeOfDay;
    }


    private Department getDepartmentByName(@RequestParam("selectedDepartment") String selectedDepartment) {

        Department department = null;

        for (Department item : departmentService.getAllDepartments()) {
            if (item.getName().equals(selectedDepartment)) {
                department = item;

                break;
            }
        }

        return department;
    }


    private VacationOverview getVacationOverview(Person person) {

        VacationOverview vacationOverview = new VacationOverview();
        vacationOverview.setDays(new ArrayList<>());
        vacationOverview.setPerson(new PersonResponse(person));
        vacationOverview.setPersonID(person.getId());

        return vacationOverview;
    }
}
