package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.blackoutperiod.BlackoutPeriodEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutPeriodDTOTest {

    @Test
    void happyPathDTOToEntity() {

        final LocalDate startDate = LocalDate.of(2025, 6, 10);
        final LocalDate endDate = LocalDate.of(2025, 6, 20);
        final LocalDate createdAt = LocalDate.of(2025, 3, 1);
        final LocalDate lastModification = LocalDate.of(2025, 3, 2);

        final BlackoutPeriodDTO dto = new BlackoutPeriodDTO("Release", startDate, endDate, createdAt, lastModification,
            false, false, List.of(1L), List.of(10000L));
        final BlackoutPeriodEntity entity = dto.toEntity(List.of(42L), List.of(7L, 8L));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getTitle()).isEqualTo("Release");
        assertThat(entity.getStartDate()).isEqualTo(startDate);
        assertThat(entity.getEndDate()).isEqualTo(endDate);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getLastModification()).isEqualTo(lastModification);
        assertThat(entity.isCompanyWide()).isFalse();
        assertThat(entity.isAllVacationTypes()).isFalse();
        assertThat(entity.getDepartmentIds()).containsExactly(42L);
        assertThat(entity.getVacationTypeIds()).containsExactlyInAnyOrder(7L, 8L);
    }
}
