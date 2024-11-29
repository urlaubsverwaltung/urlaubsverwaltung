package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.focus_shift.jollyday.core.HolidayType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isChristmasEve;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isNewYearsEve;

@Service
public class PublicHolidaysServiceImpl implements PublicHolidaysService {

    private final Map<String, HolidayManager> holidayManagers;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidaysServiceImpl(SettingsService settingsService, Map<String, HolidayManager> holidayManagers) {
        this.settingsService = settingsService;
        this.holidayManagers = holidayManagers;
    }

    @Override
    public boolean isPublicHoliday(LocalDate date, FederalState federalState) {
        if (isChristmasEve(date) || isNewYearsEve(date)) {
            return true;
        }

        return getHolidayManager(federalState)
            .map(holidayManager -> holidayManager.isHoliday(date, federalState.getCodes()))
            .orElse(false);
    }

    @Override
    public Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState, WorkingTimeSettings workingTimeSettings) {
        return getPublicHolidays(date, date, federalState, workingTimeSettings).stream().findFirst();
    }

    @Override
    public Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState) {
        return getPublicHolidays(date, date, federalState).stream().findFirst();
    }

    @Override
    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState) {
        return getPublicHolidays(from, to, federalState, getWorkingTimeSettings());
    }

    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState, WorkingTimeSettings workingTimeSettings) {
        final Locale locale = LocaleContextHolder.getLocale();

        return getHolidays(from, to, federalState).stream()
            .map(holiday -> new PublicHoliday(holiday.getDate(), getHolidayDayLength(workingTimeSettings, holiday.getDate(), federalState), holiday.getDescription(locale)))
            .toList();
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

        final Set<Holiday> holidays = getHolidayManager(federalState)
            .map(holidayManager -> holidayManager.getHolidays(from, to, PUBLIC_HOLIDAY, federalState.getCodes()))
            .orElseGet(HashSet::new);

        final DateRange requestRange = new DateRange(from, to);

        int year = from.getYear();
        while (year <= to.getYear()) {
            final LocalDate christmas = LocalDate.of(year, Month.DECEMBER, 24);
            if (requestRange.isOverlapping(new DateRange(christmas, christmas))) {
                holidays.add(new Holiday(christmas, "CHRISTMAS_EVE", PUBLIC_HOLIDAY));
            }
            final LocalDate newYear = LocalDate.of(year, Month.DECEMBER, 31);
            if (requestRange.isOverlapping(new DateRange(newYear, newYear))) {
                holidays.add(new Holiday(newYear, "NEW_YEARS_EVE", PUBLIC_HOLIDAY));
            }
            year++;
        }

        return holidays;
    }

    private Optional<HolidayManager> getHolidayManager(FederalState federalState) {
        return Optional.ofNullable(holidayManagers.get(federalState.getCountry()));
    }

    private WorkingTimeSettings getWorkingTimeSettings() {
        return settingsService.getSettings().getWorkingTimeSettings();
    }
}
