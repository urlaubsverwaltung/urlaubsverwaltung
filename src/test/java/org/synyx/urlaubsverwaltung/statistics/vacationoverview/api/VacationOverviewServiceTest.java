package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.statistics.vacationoverview.api.DayOfMonth.TypeOfDay.WORKDAY;

@ExtendWith(MockitoExtension.class)
class VacationOverviewServiceTest {

    private VacationOverviewService sut;

    @Mock
    private DepartmentService departmentService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private PublicHolidaysService publicHolidayService;

    @BeforeEach
    void setUp() {
        this.sut = new VacationOverviewService(departmentService, workingTimeService, publicHolidayService, Clock.systemUTC());
    }

    @Test
    void assertVacationOverviewsForExistingDepartment() {

        final String email = "muster@example.org";
        final Person person = new Person("test", "Muster", "Max", email);

        final String departmentName = "Admins";
        final Department department = new Department();
        department.setName(departmentName);
        department.setMembers(List.of(person));

        when(departmentService.getAllDepartments()).thenReturn(singletonList(department));
        when(workingTimeService.getFederalStateForPerson(ArgumentMatchers.eq(person), ArgumentMatchers.any(LocalDate.class))).thenReturn(BADEN_WUERTTEMBERG);
        when(publicHolidayService.getWorkingDurationOfDate(ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.any(FederalState.class))).thenReturn(DayLength.FULL.getDuration());

        final LocalDate localDate = LocalDate.parse("2017-09-01");
        final List<VacationOverviewDto> vacationOverviewDtos = sut.getVacationOverviews(departmentName, localDate.getYear(), localDate.getMonthValue());
        assertThat(vacationOverviewDtos).hasSize(1);
        assertThat(vacationOverviewDtos.get(0).getPerson().getEmail()).isEqualTo(email);
        assertThat(vacationOverviewDtos.get(0).getDays().get(0).getTypeOfDay()).isEqualTo(WORKDAY);
    }
}
