package org.synyx.urlaubsverwaltung.settings;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Objects;

public class SpecialLeaveSettingsDto {

    @Valid
    private List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems;

    public List<SpecialLeaveSettingsItemDto> getSpecialLeaveSettingsItems() {
        return specialLeaveSettingsItems;
    }

    public void setSpecialLeaveSettingsItems(List<SpecialLeaveSettingsItemDto> specialLeaveSettingsItems) {
        this.specialLeaveSettingsItems = specialLeaveSettingsItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialLeaveSettingsDto that = (SpecialLeaveSettingsDto) o;
        return Objects.equals(specialLeaveSettingsItems, that.specialLeaveSettingsItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialLeaveSettingsItems);
    }

    @Override
    public String toString() {
        return "SpecialLeaveSettingsDto{" +
            "specialLeaveSettingsItems=" + specialLeaveSettingsItems +
            '}';
    }
}
