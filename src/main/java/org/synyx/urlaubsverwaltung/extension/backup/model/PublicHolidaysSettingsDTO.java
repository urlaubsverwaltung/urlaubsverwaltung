package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

public record PublicHolidaysSettingsDTO(
    // read only, working durations of backups created before company closures existed
    @Nullable DayLengthDTO workingDurationForChristmasEve,
    @Nullable DayLengthDTO workingDurationForNewYearsEve,
    FederalStateDTO federalState
) {

    public static PublicHolidaysSettingsDTO of(PublicHolidaysSettings publicHolidaysSettings) {
        return new PublicHolidaysSettingsDTO(
            null,
            null,
            FederalStateDTO.valueOf(publicHolidaysSettings.getFederalState().name())
        );
    }

    public PublicHolidaysSettings toPublicHolidaysSettings() {
        PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setFederalState(FederalState.valueOf(federalState().name()));
        return publicHolidaysSettings;
    }
}
