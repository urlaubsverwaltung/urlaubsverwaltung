package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.Department;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

class BlackoutPeriodTest {

    @Test
    void ensureCreatedAtAndLastModificationCanBeSetAndRead() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        assertThat(blackoutPeriod.getCreatedAt()).isNull();
        assertThat(blackoutPeriod.getLastModification()).isNotNull();

        blackoutPeriod.setCreatedAt(LocalDate.of(2026, 1, 1));
        blackoutPeriod.setLastModification(LocalDate.of(2026, 2, 2));

        assertThat(blackoutPeriod.getCreatedAt()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(blackoutPeriod.getLastModification()).isEqualTo(LocalDate.of(2026, 2, 2));
    }

    @Test
    void ensureIsCompanyWideWhenNoDepartmentIsSet() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        assertThat(blackoutPeriod.isCompanyWide()).isTrue();

        final Department department = createDepartment("Vertrieb");
        department.setId(42L);
        blackoutPeriod.setDepartments(List.of(department));

        assertThat(blackoutPeriod.isCompanyWide()).isFalse();
        assertThat(blackoutPeriod.getDepartments()).containsExactly(department);
    }

    @Test
    void ensureAppliesToAllVacationTypesWhenNoVacationTypeIsSet() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        assertThat(blackoutPeriod.appliesToAllVacationTypes()).isTrue();

        final VacationType<?> vacationType = createVacationType(1L, HOLIDAY, new StaticMessageSource());
        blackoutPeriod.setVacationTypes(List.of(vacationType));

        assertThat(blackoutPeriod.appliesToAllVacationTypes()).isFalse();
        assertThat(blackoutPeriod.getVacationTypes()).containsExactly(vacationType);
    }

    @Test
    void ensureOverlapsIsInclusiveOnBothBoundaries() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));

        assertThat(blackoutPeriod.overlaps(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 20))).isTrue();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 31))).isTrue();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 19))).isFalse();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2027, 1, 6), LocalDate.of(2027, 1, 31))).isFalse();
    }

    @Test
    void ensureToString() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, 12, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, 1, 5));

        assertThat(blackoutPeriod).hasToString("BlackoutPeriod{id=1, title='Jahresabschluss', startDate=2026-12-20, endDate=2027-01-05}");
    }

    @Test
    void equals() {

        final BlackoutPeriod blackoutPeriodOne = new BlackoutPeriod();
        blackoutPeriodOne.setId(1L);

        final BlackoutPeriod blackoutPeriodOneOne = new BlackoutPeriod();
        blackoutPeriodOneOne.setId(1L);

        final BlackoutPeriod blackoutPeriodTwo = new BlackoutPeriod();
        blackoutPeriodTwo.setId(2L);

        assertThat(blackoutPeriodOne)
            .isEqualTo(blackoutPeriodOne)
            .isEqualTo(blackoutPeriodOneOne)
            .isNotEqualTo(blackoutPeriodTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void ensureNotEqualWithoutId() {

        final BlackoutPeriod blackoutPeriodOne = new BlackoutPeriod();
        final BlackoutPeriod blackoutPeriodTwo = new BlackoutPeriod();

        assertThat(blackoutPeriodOne).isNotEqualTo(blackoutPeriodTwo);
    }

    @Test
    void hashCodeTest() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);

        assertThat(blackoutPeriod.hashCode()).isEqualTo(32);
    }
}
