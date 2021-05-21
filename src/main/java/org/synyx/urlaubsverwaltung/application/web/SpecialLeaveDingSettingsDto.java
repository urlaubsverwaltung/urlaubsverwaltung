package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.application.domain.SpecialLeave;

import java.util.List;

public class SpecialLeaveDingSettingsDto {

    private List<SpecialLeave> allSpecialLeaves;

    private List<SpecialLeaveDingDto> chosenSpecialLeaves;

    public class SpecialLeaveDingDto {

        private Integer id;
        private SpecialLeave specialLeave;
        private int days;
    }
}
