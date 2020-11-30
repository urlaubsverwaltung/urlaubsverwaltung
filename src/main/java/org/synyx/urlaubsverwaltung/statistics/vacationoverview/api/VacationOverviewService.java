package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WORKDAY;

/**
 * @deprecated This service purpose was to provide information for the client side rendered vacation overview which is obsolete now.
 */
@Component
@Deprecated(since = "4.0.0", forRemoval = true)
public class VacationOverviewService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final PublicHolidaysService publicHolidayService;
    private final Clock clock;

    @Autowired
    public VacationOverviewService(DepartmentService departmentService,
                                   WorkingTimeService workingTimeService,
                                   PublicHolidaysService publicHolidayService, Clock clock) {

        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
        this.clock = clock;
    }

    public List<VacationOverviewDto> getVacationOverviews(String selectedDepartment,
                                                          Integer selectedYear,
                                                          Integer selectedMonth) {
        List<VacationOverviewDto> holidayOverviewList = new ArrayList<>();

        Department department = getDepartmentByName(selectedDepartment);

        if (department != null) {

            for (Person person : department.getMembers()) {

                LocalDate date = LocalDate.now(clock);
                int year = selectedYear != null ? selectedYear : date.getYear();
                int month = selectedMonth != null ? selectedMonth : date.getMonthValue();
                LocalDate lastDay = DateUtil.getLastDayOfMonth(year, month);

                VacationOverviewDto holidayOverview = getVacationOverview(person);

                for (int i = 1; i <= lastDay.getDayOfMonth(); i++) {

                    DayOfMonth dayOfMonth = new DayOfMonth();
                    LocalDate currentDay = LocalDate.of(year, month, i);
                    dayOfMonth.setDayText(currentDay.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                    dayOfMonth.setDayNumber(i);

                    dayOfMonth.setTypeOfDay(getTypeOfDay(person, currentDay));
                    holidayOverview.getDays().add(dayOfMonth);
                }
                holidayOverviewList.add(holidayOverview);
            }
        }
        return holidayOverviewList;
    }

    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, LocalDate currentDay) {
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

    private VacationOverviewDto getVacationOverview(Person person) {
        VacationOverviewDto vacationOverviewDto = new VacationOverviewDto();
        vacationOverviewDto.setDays(new ArrayList<>());
        vacationOverviewDto.setPerson(PersonMapper.mapToDto(person));
        vacationOverviewDto.setPersonID(person.getId());
        return vacationOverviewDto;
    }
}
