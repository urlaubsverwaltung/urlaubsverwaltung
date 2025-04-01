package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

public record PublicHolidaysSettingsDTO(
    DayLengthDTO workingDurationForChristmasEve,
    DayLengthDTO workingDurationForNewYearsEve,
    FederalStateDTO federalState
) {

    public static PublicHolidaysSettingsDTO of(PublicHolidaysSettings publicHolidaysSettings) {
        return new PublicHolidaysSettingsDTO(
            DayLengthDTO.valueOf(publicHolidaysSettings.getWorkingDurationForChristmasEve().name()),
            DayLengthDTO.valueOf(publicHolidaysSettings.getWorkingDurationForNewYearsEve().name()),
            FederalStateDTO.valueOf(publicHolidaysSettings.getFederalState().name())
        );
    }

    public PublicHolidaysSettings toPublicHolidaysSettings() {
        PublicHolidaysSettings publicHolidaysSettings = new PublicHolidaysSettings();
        publicHolidaysSettings.setWorkingDurationForChristmasEve(DayLength.valueOf(workingDurationForChristmasEve().name()));
        publicHolidaysSettings.setWorkingDurationForNewYearsEve(DayLength.valueOf(workingDurationForNewYearsEve().name()));
        publicHolidaysSettings.setFederalState(FederalState.valueOf(federalState().name()));
        return publicHolidaysSettings;
    }
}
