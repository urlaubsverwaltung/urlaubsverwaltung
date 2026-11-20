package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

public record PublicHolidaysSettingsDTO(FederalStateDTO federalState) {

    public static PublicHolidaysSettingsDTO of(PublicHolidaysSettings publicHolidaysSettings) {
        return new PublicHolidaysSettingsDTO(FederalStateDTO.valueOf(publicHolidaysSettings.getFederalState().name()));
    }

    public PublicHolidaysSettings toPublicHolidaysSettings() {
        PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(FederalState.valueOf(federalState().name()));
        return publicHolidaysSettings;
    }
}
