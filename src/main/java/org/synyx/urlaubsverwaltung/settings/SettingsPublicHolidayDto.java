package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;

import java.util.Objects;

public class SettingsPublicHolidayDto {

    private Long id;
    private PublicHolidaysSettings publicHolidaysSettings;
    private DayLength workingDurationForChristmasEve;
    private DayLength workingDurationForNewYearsEve;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicHolidaysSettings getPublicHolidaysSettings() {
        return publicHolidaysSettings;
    }

    public void setPublicHolidaysSettings(PublicHolidaysSettings publicHolidaysSettings) {
        this.publicHolidaysSettings = publicHolidaysSettings;
    }

    public DayLength getWorkingDurationForChristmasEve() {
        return workingDurationForChristmasEve;
    }

    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {
        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }

    public DayLength getWorkingDurationForNewYearsEve() {
        return workingDurationForNewYearsEve;
    }

    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {
        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SettingsPublicHolidayDto that = (SettingsPublicHolidayDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(publicHolidaysSettings, that.publicHolidaysSettings)
            && workingDurationForChristmasEve == that.workingDurationForChristmasEve
            && workingDurationForNewYearsEve == that.workingDurationForNewYearsEve;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicHolidaysSettings, workingDurationForChristmasEve, workingDurationForNewYearsEve);
    }
}
