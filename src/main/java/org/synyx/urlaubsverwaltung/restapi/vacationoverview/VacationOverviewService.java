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

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        int year = selectedYear != null ? selectedYear : LocalDate.now().getYear();
        int month = selectedMonth != null ? selectedMonth : LocalDate.now().getMonthValue();

        List<LocalDate> daysOfMonthList = getDaysOfMonth(year, month);

        if (department != null) {
            (department.getMembers()).stream().forEach(person -> {
                VacationOverview vacationOverviewForPerson = getVacationOverview(person);
                daysOfMonthList.stream().forEach(day -> {
                    DayOfMonth dayOfMonth = getDayOfMonth(person, day);
                    vacationOverviewForPerson.getDays().add(dayOfMonth);
                });
                holidayOverviewList.add(vacationOverviewForPerson);
            });
        }

        return holidayOverviewList;
    }


    private DayOfMonth getDayOfMonth(Person person, LocalDate day) {

        DayOfMonth dayOfMonth = new DayOfMonth();
        dayOfMonth.setDayText(day.toString());
        dayOfMonth.setDayNumber(day.getDayOfMonth());
        dayOfMonth.setTypeOfDay(getTypeOfDay(person, day));

        return dayOfMonth;
    }


    private List<LocalDate> getDaysOfMonth(int year, int month) {

        LocalDate date = LocalDate.of(year, month, 1);
        ValueRange rangeOfDaysOfMonth = date.range(ChronoField.DAY_OF_MONTH);

        return IntStream.rangeClosed((int) rangeOfDaysOfMonth.getMinimum(), (int) rangeOfDaysOfMonth.getMaximum())
            .boxed()
            .map(x -> LocalDate.of(year, month, x))
            .collect(Collectors.toList());
    }


    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, LocalDate currentDay) {

        DateMidnight dmDay = DateMidnight.parse(currentDay.toString());

        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, dmDay);

        if (DateUtil.isWorkDay(dmDay)
                && (publicHolidayService.getWorkingDurationOfDate(dmDay, state).longValue() > 0)) {
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
