package org.synyx.urlaubsverwaltung.companyvacation;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyVacationTest {

    @Test
    void getWorkingDurationFull() {

        final CompanyVacation companyVacation = new CompanyVacation(LocalDate.MIN, DayLength.FULL, "description");
        assertThat(companyVacation.workingDuration()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getWorkingDurationMorning() {

        final CompanyVacation companyVacation = new CompanyVacation(LocalDate.MIN, DayLength.MORNING, "description");
        assertThat(companyVacation.workingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkingDurationNoon() {

        final CompanyVacation companyVacation = new CompanyVacation(LocalDate.MIN, DayLength.NOON, "description");
        assertThat(companyVacation.workingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkingDurationZero() {

        final CompanyVacation companyVacation = new CompanyVacation(LocalDate.MIN, DayLength.ZERO, "description");
        assertThat(companyVacation.workingDuration()).isEqualByComparingTo(BigDecimal.ONE);
    }
}
