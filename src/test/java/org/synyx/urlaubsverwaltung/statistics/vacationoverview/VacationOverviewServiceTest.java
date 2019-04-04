package org.synyx.urlaubsverwaltung.statistics.vacationoverview;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.VacationOverviewService;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.holiday.DayOfMonth.TypeOfDay.WORKDAY;

public class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;

    @Before
    public void setUp() {
        this.departmentService = mock(DepartmentService.class);
        this.workingTimeService = mock(WorkingTimeService.class);
        this.publicHolidayService = mock(PublicHolidaysService.class);
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService);
    }

    @Test
    public void assertVacationOverviewsForExistingDepartment() {
        Department department = new Department();
        String departmentName = "Admins";
        department.setName(departmentName);
        String email = "muster@firma.test";
        Person person = new Person("test", "Muster", "Max", email);
        department.setMembers(singletonList(person));
        LocalDate testDate = LocalDate.parse("2017-09-01");
        FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

        when(departmentService.getAllDepartments()).thenReturn(singletonList(department));
        when(workingTimeService.getFederalStateForPerson(eq(person), any(LocalDate.class))).thenReturn(federalState);
        when(publicHolidayService.getWorkingDurationOfDate(any(LocalDate.class), any(FederalState.class))).thenReturn(DayLength.FULL.getDuration());

        List<VacationOverview> vacationOverviews =
                sut.getVacationOverviews(departmentName, testDate.getYear(), testDate.getMonthValue());

        assertThat(vacationOverviews, hasSize(1));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(email));
        assertThat(vacationOverviews.get(0).getDays().get(0).getTypeOfDay(), is(WORKDAY));
    }
}
