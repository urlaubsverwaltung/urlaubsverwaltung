package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WORKDAY;

class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;

    @BeforeEach
    void setUp() {
        this.departmentService = mock(DepartmentService.class);
        this.workingTimeService = mock(WorkingTimeService.class);
        this.publicHolidayService = mock(PublicHolidaysService.class);
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService);
    }

    @Test
    void assertVacationOverviewsForExistingDepartment() {
        Department department = new Department();
        String departmentName = "Admins";
        department.setName(departmentName);
        String email = "muster@firma.test";
        Person person = new Person("test", "Muster", "Max", email);
        department.setMembers(singletonList(person));
        LocalDate testDate = LocalDate.parse("2017-09-01");
        FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

        when(departmentService.getAllDepartments()).thenReturn(singletonList(department));
        when(workingTimeService.getFederalStateForPerson(ArgumentMatchers.eq(person), ArgumentMatchers.any(LocalDate.class))).thenReturn(federalState);
        when(publicHolidayService.getWorkingDurationOfDate(ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.any(FederalState.class))).thenReturn(DayLength.FULL.getDuration());

        List<VacationOverviewDto> vacationOverviewDtos =
            sut.getVacationOverviews(departmentName, testDate.getYear(), testDate.getMonthValue());

        assertThat(vacationOverviewDtos, Matchers.hasSize(1));
        assertThat(vacationOverviewDtos.get(0).getPerson().getEmail(), Is.is(email));
        assertThat(vacationOverviewDtos.get(0).getDays().get(0).getTypeOfDay(), Is.is(WORKDAY));
    }
}
