package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;

import static java.math.BigDecimal.TEN;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

class ApplicationForLeaveTest {

    @Test
    void ensureCreatesCorrectApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2015, 3, 3), of(2015, 3, 6), FULL, new StaticMessageSource());

        final ApplicationForLeave sut = new ApplicationForLeave(application, TEN);
        assertThat(sut.getStartDate()).isEqualTo(application.getStartDate());
        assertThat(sut.getEndDate()).isEqualTo(application.getEndDate());
        assertThat(sut.getDayLength()).isEqualTo(application.getDayLength());
        assertThat(sut.getWorkDays()).isEqualTo(TEN);
    }

    @Test
    void ensureApplicationForLeaveHasInformationAboutDayOfWeek() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application = createApplication(person, of(2016, 3, 1), of(2016, 3, 4), FULL, new StaticMessageSource());

        final ApplicationForLeave sut = new ApplicationForLeave(application, BigDecimal.valueOf(4));
        assertThat(sut.getWeekDayOfStartDate()).isEqualTo(TUESDAY);
        assertThat(sut.getWeekDayOfEndDate()).isEqualTo(FRIDAY);
    }
}
