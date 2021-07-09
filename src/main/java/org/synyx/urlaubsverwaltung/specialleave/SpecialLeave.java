package org.synyx.urlaubsverwaltung.specialleave;

public enum SpecialLeave {

    OWN_WEDDING("application.special-leave.own-wedding"),
    BIRTH_OF_A_CHILD("application.special-leave.birth-of-a-child"),
    DEATH_OF_SPOUSE_OR_CHILD("application.special-leave.death-of-spouse-or-child"),
    DEATH_OF_PARENT("application.special-leave.death-of-parent"),
    RELOCATION_FOR_OPERATIONAL_REASONS("application.special-leave.relocation-for-operational-reasons");

    private final String messageKey;

    SpecialLeave(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
