package org.synyx.urlaubsverwaltung.settings;

import java.util.List;

public class SpecialLeaveSettingsDto {

    private List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems;

    public List<SpecialLeaveSettingsItemDto> getSpecialLeaveSettingsItems() {
        return specialLeaveSettingsItems;
    }

    public void setSpecialLeaveSettingsItems(List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems) {
        this.specialLeaveSettingsItems = specialLeaveSettingsItems;
    }
}
