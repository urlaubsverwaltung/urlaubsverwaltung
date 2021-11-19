package org.synyx.urlaubsverwaltung.settings;

import java.util.List;
import java.util.Objects;

public class AbsenceTypeSettingsDto {

    private List<AbsenceTypeSettingsItemDto> items;

    public List<AbsenceTypeSettingsItemDto> getItems() {
        return items;
    }

    public void setItems(List<AbsenceTypeSettingsItemDto> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceTypeSettingsDto that = (AbsenceTypeSettingsDto) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
