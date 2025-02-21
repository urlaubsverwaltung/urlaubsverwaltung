package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;

import java.util.List;

class SpecialLeaveDtoMapper {

    private SpecialLeaveDtoMapper() {
        // ok
    }

    static SpecialLeaveDto mapToSpecialLeaveSettingsDto(List<SpecialLeaveSettingsItem> specialLeaveItems) {
        final List<SpecialLeaveItemDto> specialLeaveItemDtos = specialLeaveItems.stream()
            .map(SpecialLeaveDtoMapper::toSpecialLeaveItemDto)
            .toList();
        final SpecialLeaveDto specialLeaveDto = new SpecialLeaveDto();
        specialLeaveDto.setSpecialLeaveItems(specialLeaveItemDtos);
        return specialLeaveDto;
    }

    private static SpecialLeaveItemDto toSpecialLeaveItemDto(SpecialLeaveSettingsItem item) {
        final SpecialLeaveItemDto specialLeaveItemDto = new SpecialLeaveItemDto();
        specialLeaveItemDto.setActive(item.active());
        specialLeaveItemDto.setMessageKey(item.messageKey());
        specialLeaveItemDto.setDays(item.days());
        return specialLeaveItemDto;
    }
}
