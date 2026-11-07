package org.synyx.urlaubsverwaltung.companyclosure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;

import static java.time.Month.DECEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@SpringBootTest
@Transactional
class CompanyClosureServiceImplIT extends SingleTenantTestContainersBase {

    @Autowired
    private CompanyClosureService sut;

    @Test
    void defaultsExistAfterStartup() {
        assertThat(sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 1), LocalDate.of(2026, DECEMBER, 31)))
            .containsExactly(
                new CompanyClosure(LocalDate.of(2026, DECEMBER, 24), NOON, "Heiligabend"),
                new CompanyClosure(LocalDate.of(2026, DECEMBER, 31), NOON, "Silvester"));
    }

    @Test
    void changePersistsANewVersion() {
        final Year year = Year.now();

        sut.changeCompanyClosure(MonthDay.of(DECEMBER, 24), FULL, year.atDay(1));

        final var christmasEve = year.atMonthDay(MonthDay.of(DECEMBER, 24));
        final var lastYearsChristmasEve = year.minusYears(1).atMonthDay(MonthDay.of(DECEMBER, 24));
        assertThat(sut.getCompanyClosures(christmasEve, christmasEve)).extracting(CompanyClosure::dayLength).containsExactly(FULL);
        assertThat(sut.getCompanyClosures(lastYearsChristmasEve, lastYearsChristmasEve)).extracting(CompanyClosure::dayLength).containsExactly(NOON);
    }
}
