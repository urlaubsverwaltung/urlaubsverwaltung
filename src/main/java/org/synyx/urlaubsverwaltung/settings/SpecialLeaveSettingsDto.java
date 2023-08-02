package org.synyx.urlaubsverwaltung.settings;

import jakarta.validation.Valid;

import java.util.List;

public class SpecialLeaveSettingsDto {

    @Valid
    private List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems;

    public List<SpecialLeaveSettingsItemDto> getSpecialLeaveSettingsItems() {
        return specialLeaveSettingsItems;
    }

    public void setSpecialLeaveSettingsItems(List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems) {
        this.specialLeaveSettingsItems = specialLeaveSettingsItems;
    }
}
