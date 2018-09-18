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

import java.time.LocalDate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WORKDAY;


public class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    private Department department;
    private Person person;

    @Before
    public void setUp() throws Exception {

        Person person2;
        DepartmentService departmentService;
        WorkingTimeService workingTimeService;
        PublicHolidaysService publicHolidayService;
        SettingsServiceImpl settingsServiceImpl;

        settingsServiceImpl = mock(SettingsServiceImpl.class);
        departmentService = mock(DepartmentService.class);
        workingTimeService = mock(WorkingTimeService.class);

        publicHolidayService = new PublicHolidaysService(settingsServiceImpl);
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService);

        this.person = TestDataCreator.createPerson();
        person2 = TestDataCreator.createPerson("mamu", "Max", "Mustermann", "max@mustermann.de");
        this.department = TestDataCreator.createDepartment("Admins");
        this.department.setMembers(Arrays.asList(person, person2));

        FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(department));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(DateMidnight.class))).thenReturn(
            federalState);
        when(settingsServiceImpl.getSettings()).thenReturn(new Settings());
    }


    @Test
    public void assertVacationOverviewsForExistingDepartment() throws Exception {

        LocalDate testDate = LocalDate.parse("2017-09-01");

        List<VacationOverview> vacationOverviews = sut.getVacationOverviews(this.department.getName(),
                testDate.getYear(), testDate.getMonthValue());

        assertThat(vacationOverviews, hasSize(2));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(this.person.getEmail()));
        assertThat(vacationOverviews.get(0).getDays().get(0).getTypeOfDay(), is(WORKDAY));
    }


    @Test
    public void ensureHolidaysAreMarkedCorrectlyAsWeekend() throws Exception {

        LocalDate testDate = LocalDate.parse("2017-12-01");

        List<VacationOverview> vacationOverviews = sut.getVacationOverviews(this.department.getName(),
                testDate.getYear(), testDate.getMonthValue());

        assertThat(vacationOverviews, hasSize(2));
        assertThat(vacationOverviews.get(0).getPerson().getEmail(), is(this.person.getEmail()));
        assertThat(vacationOverviews.get(0).getDays().get(25).getTypeOfDay(), is(WEEKEND));
    }
}
