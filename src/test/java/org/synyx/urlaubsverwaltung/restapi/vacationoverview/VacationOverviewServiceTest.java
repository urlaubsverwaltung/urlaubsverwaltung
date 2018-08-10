package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import org.joda.time.DateMidnight;

import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsServiceImpl;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WORKDAY;


public class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    private DepartmentService departmentService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;
    private SettingsServiceImpl settingsServiceImpl;
    private Department department;
    private Person person;

    @Before
    public void setUp() throws Exception {

        this.settingsServiceImpl = mock(SettingsServiceImpl.class);
        this.departmentService = mock(DepartmentService.class);
        this.workingTimeService = mock(WorkingTimeService.class);

        this.publicHolidayService = new PublicHolidaysService(settingsServiceImpl);
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService);

        this.person = TestDataCreator.createPerson();
        this.department = TestDataCreator.createDepartment("Admins");
        this.department.setMembers(Collections.singletonList(person));

        FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(department));
        when(workingTimeService.getFederalStateForPerson(eq(person), any(DateMidnight.class))).thenReturn(
            federalState);
        when(settingsServiceImpl.getSettings()).thenReturn(new Settings());
    }


    @Test
    public void assertVacationOverviewsForExistingDepartment() throws Exception {

        DateMidnight testDate = DateMidnight.parse("2017-09-01");

        List<VacationOverview> vacationOverviews = sut.getVacationOverviews(this.department.getName(),
                testDate.getYear(), testDate.getMonthOfYear());

        assertThat(vacationOverviews, hasSize(1));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(this.person.getEmail()));
        assertThat(vacationOverviews.get(0).getDays().get(0).getTypeOfDay(), is(WORKDAY));
    }


    @Test
    public void ensureHolidaysAreMarkedCorrectlyAsWeekend() throws Exception {

        DateMidnight testDate = DateMidnight.parse("2017-12-01");

        List<VacationOverview> vacationOverviews = sut.getVacationOverviews(this.department.getName(),
                testDate.getYear(), testDate.getMonthOfYear());

        assertThat(vacationOverviews, hasSize(1));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(this.person.getEmail()));
        assertThat(vacationOverviews.get(0).getDays().get(25).getTypeOfDay(), is(WEEKEND));
    }
}
