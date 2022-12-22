package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SpecialLeaveDtoMapperTest {

    @Test
    void mapToSpecialLeaveSettingsDto() {
        Long id = 1L;
        Boolean active = true;
        String messageKey = "";
        Integer days = 2;

        SpecialLeaveSettingsItem specialLeaveSettingsItem = new SpecialLeaveSettingsItem(id, active, messageKey, days);

        List<SpecialLeaveSettingsItem> specialLeaveItems = List.of(specialLeaveSettingsItem);
        final SpecialLeaveDto specialLeaveDto = SpecialLeaveDtoMapper.mapToSpecialLeaveSettingsDto(specialLeaveItems);
        assertThat(specialLeaveDto.getSpecialLeaveItems())
            .extracting("active", "messageKey", "days")
            .contains(tuple(active, messageKey, days));
    }
}
