package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsEmbeddable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isChristmasEve;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isNewYearsEve;

@Component
public class PublicHolidaysService {

    private final HolidayManager manager;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidaysService(SettingsService settingsService, HolidayManager holidayManager) {
        this.settingsService = settingsService;
        this.manager = holidayManager;
    }

    /**
     * Returns the working duration for a date: may be full day (1.0) for a non public holiday or zero (0.0) for a
     * public holiday. The working duration for Christmas Eve and New Year's Eve are configured in the business
     * properties; normally the working duration for these holidays is a half day (0.5)
     *
     * @param date         to get working duration for
     * @param federalState the federal state to consider holiday settings for
     * @return working duration of the given date
     */
    public BigDecimal getWorkingDurationOfDate(LocalDate date, FederalState federalState) {
        return getAbsenceTypeOfDate(date, federalState).getInverse().getDuration();
    }

    public DayLength getAbsenceTypeOfDate(LocalDate date, FederalState federalState) {

        final Settings settings = settingsService.getSettings();
        final WorkingTimeSettingsEmbeddable workingTimeSettings = settings.getWorkingTimeSettings();

        return getHolidayDayLength(workingTimeSettings, date, federalState);
    }

    public List<Holiday> getHolidays(final LocalDate from, final LocalDate to, FederalState federalState) {
        return List.copyOf(manager.getHolidays(from, to, federalState.getCodes()));
    }

    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState) {
        final Settings settings = settingsService.getSettings();
        final WorkingTimeSettingsEmbeddable workingTimeSettings = settings.getWorkingTimeSettings();

        return getHolidays(from, to, federalState).stream()
            .map(holiday -> new PublicHoliday(holiday.getDate(), getHolidayDayLength(workingTimeSettings, holiday.getDate(), federalState)))
            .collect(toUnmodifiableList());
    }

    private DayLength getHolidayDayLength(WorkingTimeSettingsEmbeddable workingTimeSettingsEmbeddable, LocalDate date, FederalState federalState) {
        DayLength workingTime = FULL;
        if (isPublicHoliday(date, federalState)) {
            if (isChristmasEve(date)) {
                workingTime = workingTimeSettingsEmbeddable.getWorkingDurationForChristmasEve();
            } else if (isNewYearsEve(date)) {
                workingTime = workingTimeSettingsEmbeddable.getWorkingDurationForNewYearsEve();
            } else {
                workingTime = ZERO;
            }
        }

        return workingTime.getInverse();
    }

    private boolean isPublicHoliday(LocalDate date, FederalState federalState) {
        return manager.isHoliday(date, federalState.getCodes());
    }
}
