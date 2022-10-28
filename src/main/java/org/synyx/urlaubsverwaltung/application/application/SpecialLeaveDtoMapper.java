package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;

import java.util.List;

import static java.util.stream.Collectors.toList;

class SpecialLeaveDtoMapper {

    private SpecialLeaveDtoMapper() {
        // ok
    }

    static SpecialLeaveDto mapToSpecialLeaveSettingsDto(List<SpecialLeaveSettingsItem> specialLeaveItems) {
        final List<SpecialLeaveItemDto> specialLeaveItemDtos = specialLeaveItems.stream()
            .map(SpecialLeaveDtoMapper::toSpecialLeaveItemDto)
            .collect(toList());
        final SpecialLeaveDto specialLeaveDto = new SpecialLeaveDto();
        specialLeaveDto.setSpecialLeaveItems(specialLeaveItemDtos);
        return specialLeaveDto;
    }

    private static SpecialLeaveItemDto toSpecialLeaveItemDto(SpecialLeaveSettingsItem item) {
        final SpecialLeaveItemDto specialLeaveItemDto = new SpecialLeaveItemDto();
        specialLeaveItemDto.setActive(item.isActive());
        specialLeaveItemDto.setMessageKey(item.getMessageKey());
        specialLeaveItemDto.setDays(item.getDays());
        return specialLeaveItemDto;
    }
}
