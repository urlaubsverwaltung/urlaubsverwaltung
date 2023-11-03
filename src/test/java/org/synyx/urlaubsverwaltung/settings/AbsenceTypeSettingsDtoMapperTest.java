package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.util.List;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
import static org.synyx.urlaubsverwaltung.settings.AbsenceTypeSettingsDtoMapper.mapToAbsenceTypeItemSettingDto;

@ExtendWith(MockitoExtension.class)
class AbsenceTypeSettingsDtoMapperTest {

    @Mock
    private MessageSource messageSource;

    @Test
    void mapToAbsenceTypeItemSettingDtoTest() {

        when(messageSource.getMessage("messageKey", new Object[]{}, GERMAN)).thenReturn("label");

        final VacationType<?> vacationType = ProvidedVacationType.builder(messageSource)
            .color(YELLOW)
            .id(42L)
            .active(true)
            .requiresApprovalToApply(false)
            .messageKey("messageKey")
            .category(HOLIDAY)
            .build();

        List<VacationType<?>> vacationTypes = List.of(vacationType);
        final AbsenceTypeSettingsDto absenceTypeSettingsDto = mapToAbsenceTypeItemSettingDto(vacationTypes, GERMAN);

        assertThat(absenceTypeSettingsDto.getItems())
            .extracting(
                AbsenceTypeSettingsItemDto::getColor,
                AbsenceTypeSettingsItemDto::getId,
                AbsenceTypeSettingsItemDto::isActive,
                AbsenceTypeSettingsItemDto::isRequiresApprovalToApply,
                AbsenceTypeSettingsItemDto::getLabel,
                AbsenceTypeSettingsItemDto::getCategory)
            .contains(tuple(YELLOW, 42L, true, false, "label", HOLIDAY));
    }
}
