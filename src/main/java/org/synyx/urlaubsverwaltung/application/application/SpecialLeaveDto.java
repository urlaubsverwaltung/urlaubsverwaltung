package org.synyx.urlaubsverwaltung.application.application;

import java.util.List;

public class SpecialLeaveDto {

    private List<SpecialLeaveItemDto> specialLeaveItems;

    public List<SpecialLeaveItemDto> getSpecialLeaveItems() {
        return specialLeaveItems;
    }

    public void setSpecialLeaveItems(List<SpecialLeaveItemDto> specialLeaveItems) {
        this.specialLeaveItems = specialLeaveItems;
    }

    @Override
    public String toString() {
        return "SpecialLeaveDto{" +
            "specialLeaveItems=" + specialLeaveItems +
            '}';
    }
}
