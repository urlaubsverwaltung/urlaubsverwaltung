package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
import static org.synyx.urlaubsverwaltung.settings.AbsenceTypeSettingsDtoMapper.mapToAbsenceTypeItemSettingDto;

class AbsenceTypeSettingsDtoMapperTest {

    @Test
    void mapToAbsenceTypeItemSettingDtoTest() {

        final VacationType vacationType = VacationType.builder()
            .color(YELLOW)
            .id(42L)
            .active(true)
            .requiresApprovalToApply(false)
            .messageKey("messageKey")
            .category(HOLIDAY)
            .build();

        List<VacationType> vacationTypes = List.of(vacationType);
        final AbsenceTypeSettingsDto absenceTypeSettingsDto = mapToAbsenceTypeItemSettingDto(vacationTypes);

        assertThat(absenceTypeSettingsDto.getItems())
            .extracting(
                AbsenceTypeSettingsItemDto::getColor,
                AbsenceTypeSettingsItemDto::getId,
                AbsenceTypeSettingsItemDto::isActive,
                AbsenceTypeSettingsItemDto::isRequiresApprovalToApply,
                AbsenceTypeSettingsItemDto::getMessageKey,
                AbsenceTypeSettingsItemDto::getCategory)
            .contains(tuple(YELLOW, 42L, true, false, "messageKey", HOLIDAY));
    }
}
