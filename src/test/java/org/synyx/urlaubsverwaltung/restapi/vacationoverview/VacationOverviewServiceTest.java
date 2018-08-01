package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.DepartmentBuilder;
import org.synyx.urlaubsverwaltung.test.PersonBuilder;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WORKDAY;


public class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;

    @Before
    public void setUp() throws Exception {

        this.departmentService = mock(DepartmentService.class);
        this.workingTimeService = mock(WorkingTimeService.class);
        this.publicHolidayService = mock(PublicHolidaysService.class);
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService);
    }


    @Test
    public void assertVacationOverviewsForExistingDepartment() throws Exception {

        String departmentName = "Admins";
        String email = "muster@firma.test";

        Person person = new PersonBuilder().build()
                .withName("Max", "Muster")
                .withEmail(email)
                .withLoginName("test")
                .get();
        Department department = new DepartmentBuilder().build()
                .withName("Admins")
                .withMembers(Arrays.asList(person))
                .get();

        DateMidnight testDate = DateMidnight.parse("2017-09-01");
        FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(department));
        when(workingTimeService.getFederalStateForPerson(eq(person), any(DateMidnight.class))).thenReturn(
            federalState);
        when(publicHolidayService.getWorkingDurationOfDate(any(DateMidnight.class), any(FederalState.class)))
            .thenReturn(DayLength.FULL.getDuration());

        List<VacationOverview> vacationOverviews = sut.getVacationOverviews(departmentName, testDate.getYear(),
                testDate.getMonthOfYear());

        assertThat(vacationOverviews, hasSize(1));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(email));
        assertThat(vacationOverviews.get(0).getDays().get(0).getTypeOfDay(), is(WORKDAY));
    }
}
