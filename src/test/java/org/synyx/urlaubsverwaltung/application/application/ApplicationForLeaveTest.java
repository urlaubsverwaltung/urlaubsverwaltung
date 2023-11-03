package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.TEN;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveTest {

    @Mock
    private WorkDaysCountService workDaysCountService;

    @Test
    void ensureCreatesCorrectApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2015, 3, 3), of(2015, 3, 6), FULL, new StaticMessageSource());

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(TEN);

        final ApplicationForLeave sut = new ApplicationForLeave(application, workDaysCountService);
        assertThat(sut.getStartDate()).isEqualTo(application.getStartDate());
        assertThat(sut.getEndDate()).isEqualTo(application.getEndDate());
        assertThat(sut.getDayLength()).isEqualTo(application.getDayLength());
        assertThat(sut.getWorkDays()).isEqualTo(TEN);

        verify(workDaysCountService).getWorkDaysCount(application.getDayLength(), application.getStartDate(), application.getEndDate(), person);
    }

    @Test
    void ensureApplicationForLeaveHasInformationAboutDayOfWeek() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2016, 3, 1), of(2016, 3, 4), FULL, new StaticMessageSource());

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.valueOf(4));

        final ApplicationForLeave sut = new ApplicationForLeave(application, workDaysCountService);
        assertThat(sut.getWeekDayOfStartDate()).isEqualTo(TUESDAY);
        assertThat(sut.getWeekDayOfEndDate()).isEqualTo(FRIDAY);
    }
}
