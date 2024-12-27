package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class SpecialLeaveSettingsDtoMapper {

    private SpecialLeaveSettingsDtoMapper() {
    }

    public static List<SpecialLeaveSettingsItem> mapToSpecialLeaveSettingsItems(List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItemDto) {
        return specialLeaveSettingsItemDto.stream()
            .map(toSpecialLeaveSettingsItem())
            .collect(toList());
    }

    public static SpecialLeaveSettingsDto mapToSpecialLeaveSettingsDto(List<SpecialLeaveSettingsItem> specialLeaveSettingsItems) {
        final List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItemDtos = specialLeaveSettingsItems.stream()
            .map(item -> {
                final SpecialLeaveSettingsItemDto specialLeaveSettingsItemDto = new SpecialLeaveSettingsItemDto();
                specialLeaveSettingsItemDto.setId(item.getId());
                specialLeaveSettingsItemDto.setActive(item.isActive());
                specialLeaveSettingsItemDto.setMessageKey(item.getMessageKey());
                specialLeaveSettingsItemDto.setDays(item.getDays());
                return specialLeaveSettingsItemDto;
            })
            .collect(toList());
        final SpecialLeaveSettingsDto specialLeaveSettingsDto = new SpecialLeaveSettingsDto();
        specialLeaveSettingsDto.setSpecialLeaveSettingsItems(specialLeaveSettingsItemDtos);
        return specialLeaveSettingsDto;
    }

    private static Function<SpecialLeaveSettingsItemDto, SpecialLeaveSettingsItem> toSpecialLeaveSettingsItem() {
        return dto -> new SpecialLeaveSettingsItem(dto.getId(), dto.isActive(), dto.getMessageKey(), dto.getDays());
    }
}
