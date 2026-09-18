package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutPeriodEntityTest {

    @Test
    void ensurePropertiesCanBeSetAndRead() {

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getLastModification()).isNotNull();

        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));
        entity.setCreatedAt(LocalDate.of(2026, 1, 1));
        entity.setLastModification(LocalDate.of(2026, 2, 2));
        entity.setDepartmentIds(Set.of(42L));
        entity.setVacationTypeIds(Set.of(1L));

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTitle()).isEqualTo("Jahresabschluss");
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 20));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(entity.getLastModification()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(entity.getDepartmentIds()).containsExactly(42L);
        assertThat(entity.getVacationTypeIds()).containsExactly(1L);
    }

    @Test
    void ensureToString() {

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);
        entity.setTitle("Jahresabschluss");
        entity.setStartDate(LocalDate.of(2026, 12, 20));
        entity.setEndDate(LocalDate.of(2027, 1, 5));

        assertThat(entity).hasToString("BlackoutPeriodEntity{id=1, title='Jahresabschluss', startDate=2026-12-20, endDate=2027-01-05}");
    }

    @Test
    void equals() {

        final BlackoutPeriodEntity entityOne = new BlackoutPeriodEntity();
        entityOne.setId(1L);

        final BlackoutPeriodEntity entityOneOne = new BlackoutPeriodEntity();
        entityOneOne.setId(1L);

        final BlackoutPeriodEntity entityTwo = new BlackoutPeriodEntity();
        entityTwo.setId(2L);

        assertThat(entityOne)
            .isEqualTo(entityOne)
            .isEqualTo(entityOneOne)
            .isNotEqualTo(entityTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void ensureNotEqualWithoutId() {

        final BlackoutPeriodEntity entityOne = new BlackoutPeriodEntity();
        final BlackoutPeriodEntity entityTwo = new BlackoutPeriodEntity();

        assertThat(entityOne).isNotEqualTo(entityTwo);
    }

    @Test
    void hashCodeTest() {

        final BlackoutPeriodEntity entity = new BlackoutPeriodEntity();
        entity.setId(1L);

        assertThat(entity.hashCode()).isEqualTo(32);
    }
}
