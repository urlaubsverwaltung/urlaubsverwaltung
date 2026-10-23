package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

class BlackoutPeriodTest {

    @Test
    void ensureCreatedAtAndLastModificationCanBeSetAndRead() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        assertThat(blackoutPeriod.getCreatedAt()).isNull();
        assertThat(blackoutPeriod.getLastModification()).isNotNull();

        blackoutPeriod.setCreatedAt(LocalDate.of(2026, JANUARY, 1));
        blackoutPeriod.setLastModification(LocalDate.of(2026, FEBRUARY, 2));

        assertThat(blackoutPeriod.getCreatedAt()).isEqualTo(LocalDate.of(2026, JANUARY, 1));
        assertThat(blackoutPeriod.getLastModification()).isEqualTo(LocalDate.of(2026, FEBRUARY, 2));
    }

    @Test
    void isCompanyWideReflectsTheFlagAndNotTheDepartments() {
        final BlackoutPeriod sut = new BlackoutPeriod();
        sut.setDepartments(List.of());

        assertThat(sut.isCompanyWide()).isFalse();

        sut.setCompanyWide(true);
        assertThat(sut.isCompanyWide()).isTrue();
    }

    @Test
    void appliesToAllVacationTypesReflectsTheFlagAndNotTheVacationTypes() {
        final BlackoutPeriod sut = new BlackoutPeriod();
        sut.setVacationTypes(List.of());

        assertThat(sut.appliesToAllVacationTypes()).isFalse();

        sut.setAllVacationTypes(true);
        assertThat(sut.appliesToAllVacationTypes()).isTrue();
    }

    @Test
    void hasNoRemainingDepartmentsWhenScopedToDepartmentsButNoneIsLeft() {
        final BlackoutPeriod sut = new BlackoutPeriod();
        sut.setCompanyWide(false);
        sut.setDepartments(List.of());
        assertThat(sut.hasNoRemainingDepartments()).isTrue();

        sut.setCompanyWide(true);
        assertThat(sut.hasNoRemainingDepartments()).isFalse();
    }

    @Test
    void ensureOverlapsIsInclusiveOnBothBoundaries() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setStartDate(LocalDate.of(2026, DECEMBER, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, JANUARY, 5));

        assertThat(blackoutPeriod.overlaps(LocalDate.of(2026, DECEMBER, 1), LocalDate.of(2026, DECEMBER, 20))).isTrue();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2027, JANUARY, 5), LocalDate.of(2027, JANUARY, 31))).isTrue();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2026, DECEMBER, 1), LocalDate.of(2026, DECEMBER, 19))).isFalse();
        assertThat(blackoutPeriod.overlaps(LocalDate.of(2027, JANUARY, 6), LocalDate.of(2027, JANUARY, 31))).isFalse();
    }

    @Test
    void ensureToString() {

        final BlackoutPeriod blackoutPeriod = new BlackoutPeriod();
        blackoutPeriod.setId(1L);
        blackoutPeriod.setTitle("Jahresabschluss");
        blackoutPeriod.setStartDate(LocalDate.of(2026, DECEMBER, 20));
        blackoutPeriod.setEndDate(LocalDate.of(2027, JANUARY, 5));

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
