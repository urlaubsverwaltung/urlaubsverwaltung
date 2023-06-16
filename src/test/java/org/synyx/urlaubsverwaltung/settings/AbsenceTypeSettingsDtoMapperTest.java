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

        final VacationType vacationType = new VacationType();
        vacationType.setColor(YELLOW);
        vacationType.setId(42);
        vacationType.setActive(true);
        vacationType.setRequiresApprovalToApply(false);
        vacationType.setMessageKey("messageKey");
        vacationType.setCategory(HOLIDAY);

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
            .contains(tuple(YELLOW, 42, true, false, "messageKey", HOLIDAY));
    }
}
