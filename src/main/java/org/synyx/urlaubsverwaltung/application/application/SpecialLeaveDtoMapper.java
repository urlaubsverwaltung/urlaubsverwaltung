package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class SpecialLeaveDtoMapper {

    private SpecialLeaveDtoMapper() {
    }

    public static SpecialLeaveDto mapToSpecialLeaveSettingsDto(List<SpecialLeaveSettingsItem> specialLeaveItems) {

        final List<SpecialLeaveItemDto> specialLeaveItemDtos = specialLeaveItems.stream()
            .map(toSpecialLeaveItemDto())
            .collect(toList());

        final SpecialLeaveDto specialLeaveDto = new SpecialLeaveDto();
        specialLeaveDto.setSpecialLeaveItems(specialLeaveItemDtos);
        return specialLeaveDto;
    }

    private static Function<SpecialLeaveSettingsItem, SpecialLeaveItemDto> toSpecialLeaveItemDto() {
        return item -> {
            final SpecialLeaveItemDto specialLeaveItemDto = new SpecialLeaveItemDto();
            specialLeaveItemDto.setActive(item.isActive());
            specialLeaveItemDto.setMessageKey(item.getMessageKey());
            specialLeaveItemDto.setDays(item.getDays());
            return specialLeaveItemDto;
        };
    }
}
