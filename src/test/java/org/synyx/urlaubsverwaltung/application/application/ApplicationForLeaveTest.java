package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.math.BigDecimal.TEN;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

class ApplicationForLeaveTest {

    @Test
    void ensureCreatesCorrectApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2015, 3, 3), of(2015, 3, 6), FULL, new StaticMessageSource());

        final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
        workDaysByYear.put(2015, TEN);

        final ApplicationForLeave sut = new ApplicationForLeave(application, workDaysByYear);
        assertThat(sut.getStartDate()).isEqualTo(application.getStartDate());
        assertThat(sut.getEndDate()).isEqualTo(application.getEndDate());
        assertThat(sut.getDayLength()).isEqualTo(application.getDayLength());
        assertThat(sut.getWorkDays()).isEqualTo(TEN);
        assertThat(sut.getWorkDaysByYear()).containsExactly(entry(2015, TEN));
    }

    @Test
    void ensureWorkDaysIsSumOfWorkDaysByYearForPeriodSpanningTwoYears() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2025, 12, 29), of(2026, 1, 2), FULL, new StaticMessageSource());

        final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
        workDaysByYear.put(2025, BigDecimal.valueOf(3));
        workDaysByYear.put(2026, BigDecimal.valueOf(2));

        final ApplicationForLeave sut = new ApplicationForLeave(application, workDaysByYear);
        assertThat(sut.getWorkDaysByYear()).containsExactly(entry(2025, BigDecimal.valueOf(3)), entry(2026, BigDecimal.valueOf(2)));
        assertThat(sut.getWorkDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void ensureApplicationForLeaveHasInformationAboutDayOfWeek() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2016, 3, 1), of(2016, 3, 4), FULL, new StaticMessageSource());

        final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
        workDaysByYear.put(2016, BigDecimal.valueOf(4));

        final ApplicationForLeave sut = new ApplicationForLeave(application, workDaysByYear);
        assertThat(sut.getWeekDayOfStartDate()).isEqualTo(TUESDAY);
        assertThat(sut.getWeekDayOfEndDate()).isEqualTo(FRIDAY);
    }
}
