package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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
     * Returns the public holiday information for a date and the federal state.
     * If there is no public holiday at the given date the return value is an empty optional, otherwise
     * the public holiday will be returned.
     *
     * @param date         to get public holiday for
     * @param federalState the federal state to consider holiday settings for
     * @return the public holiday if there is one at the given date, otherwise empty optional
     */
    public Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState) {
        return getPublicHolidays(date, date, federalState).stream().findFirst();
    }

    /**
     * Returns a list of public holiday information for the given date range (inclusive from and to) and the federal state.
     * If there is no public holiday at the given date range the return value is an empty list, otherwise
     * the public holidays will be returned.
     *
     * @param from         to get public holiday from
     * @param to           to get public holiday to
     * @param federalState the federal state to consider holiday settings for
     * @return a list of public holiday if there are any for the given date range, otherwise empty list
     */
    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState) {
        final WorkingTimeSettings workingTimeSettings = getWorkingTimeSettings();
        final Locale locale = LocaleContextHolder.getLocale();

        return getHolidays(from, to, federalState).stream()
            .map(holiday -> new PublicHoliday(holiday.getDate(), getHolidayDayLength(workingTimeSettings, holiday.getDate(), federalState), holiday.getDescription(locale)))
            .collect(toUnmodifiableList());
    }

    private DayLength getHolidayDayLength(WorkingTimeSettings workingTimeSettings, LocalDate date, FederalState federalState) {
        DayLength workingTime = FULL;
        if (isPublicHoliday(date, federalState)) {
            if (isChristmasEve(date)) {
                workingTime = workingTimeSettings.getWorkingDurationForChristmasEve();
            } else if (isNewYearsEve(date)) {
                workingTime = workingTimeSettings.getWorkingDurationForNewYearsEve();
            } else {
                workingTime = ZERO;
            }
        }

        return workingTime.getInverse();
    }

    private Set<Holiday> getHolidays(final LocalDate from, final LocalDate to, FederalState federalState) {
        return manager.getHolidays(from, to, federalState.getCodes());
    }

    private boolean isPublicHoliday(LocalDate date, FederalState federalState) {
        return manager.isHoliday(date, federalState.getCodes());
    }

    private WorkingTimeSettings getWorkingTimeSettings() {
        return settingsService.getSettings().getWorkingTimeSettings();
    }
}
