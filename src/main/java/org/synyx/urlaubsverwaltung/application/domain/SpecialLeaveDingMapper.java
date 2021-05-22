package org.synyx.urlaubsverwaltung.application.domain;

import org.synyx.urlaubsverwaltung.application.dao.SpecialLeaveEntity;

public final class SpecialLeaveDingMapper {

    private SpecialLeaveDingMapper() {
    }

    public static SpecialLeaveDing mapToSpecialLeaveDing(SpecialLeaveEntity specialLeave) {

        SpecialLeaveDing specialLeaveDing = new SpecialLeaveDing();
        specialLeaveDing.setSpecialLeave(specialLeave.getSpecialLeave());
        specialLeaveDing.setDays(specialLeave.getDays());
        specialLeaveDing.setId(specialLeave.getId());

        return specialLeaveDing;
    }
}
