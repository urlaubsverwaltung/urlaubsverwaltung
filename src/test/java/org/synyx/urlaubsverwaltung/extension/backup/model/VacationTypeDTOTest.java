package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VacationTypeDTOTest {

    @Test
    void convertsToVacationTypeEntity() {
        final VacationTypeDTO dto = new VacationTypeDTO(1L, true, VacationTypeCategoryDTO.HOLIDAY, true, true, VacationTypeColorDTO.BLUE, true, false, "messageKey", Map.of(Locale.ENGLISH, "Holiday"));

        final VacationTypeEntity entity = dto.toVacationType();

        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCategory()).isEqualTo(dto.vacationTypeCategory().toVacationCategory());
        assertThat(entity.isRequiresApprovalToApply()).isEqualTo(dto.requiresApprovalToApply());
        assertThat(entity.isRequiresApprovalToCancel()).isEqualTo(dto.requiresApprovalToCancel());
        assertThat(entity.getColor()).isEqualTo(dto.color().toVacationTypeColor());
        assertThat(entity.isVisibleToEveryone()).isEqualTo(dto.visibleToEveryone());
        assertThat(entity.isCustom()).isEqualTo(dto.customType());
        assertThat(entity.getMessageKey()).isEqualTo(dto.messageKey());
        assertThat(entity.getLabelByLocale()).isEqualTo(dto.labels());
    }

}
