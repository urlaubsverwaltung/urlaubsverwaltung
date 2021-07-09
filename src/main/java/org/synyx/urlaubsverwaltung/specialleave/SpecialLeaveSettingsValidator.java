package org.synyx.urlaubsverwaltung.specialleave;

import org.springframework.validation.Errors;

public class SpecialLeaveSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private SpecialLeaveSettingsValidator() {
        // private
    }

    public static Errors validateSpecialLeavceSettings(SpecialLeaveSettingsDto specialLeaveSettingsDto, Errors errors) {


        if(specialLeaveSettingsDto.getBirthOfChildDays() == null) {
            errors.rejectValue("birthOfChildDays", ERROR_MANDATORY_FIELD);
        }
        if(specialLeaveSettingsDto.getDeathOfParentDays() == null) {
            errors.rejectValue("deathOfParentDays", ERROR_MANDATORY_FIELD);
        }
        if(specialLeaveSettingsDto.getDeathOfSpuseOrChildDays() == null) {
            errors.rejectValue("deathOfSpuseOrChildDays", ERROR_MANDATORY_FIELD);
        }
        if(specialLeaveSettingsDto.getRelocationForOperationalReasonsDays() == null) {
            errors.rejectValue("relocationForOperationalReasonsDays", ERROR_MANDATORY_FIELD);
        }
        if(specialLeaveSettingsDto.getOwnWeddingDays() == null) {
            errors.rejectValue("ownWeddingDays", ERROR_MANDATORY_FIELD);
        }

        return errors;
    }
}
