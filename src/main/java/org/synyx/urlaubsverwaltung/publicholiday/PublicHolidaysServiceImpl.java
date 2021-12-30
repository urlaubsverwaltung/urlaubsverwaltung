package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
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

@Service
public class PublicHolidaysServiceImpl implements PublicHolidaysService {

    private final HolidayManager manager;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidaysServiceImpl(SettingsService settingsService, HolidayManager holidayManager) {
        this.settingsService = settingsService;
        this.manager = holidayManager;
    }

    @Override
    public Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState) {
        return getPublicHolidays(date, date, federalState).stream().findFirst();
    }

    @Override
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
