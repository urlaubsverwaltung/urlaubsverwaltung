package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.focus_shift.jollyday.core.HolidayType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DateLimits.requireCalculableDuration;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@Service
public class PublicHolidaysServiceImpl implements PublicHolidaysService {

    private final Map<String, HolidayManager> holidayManagers;

    @Autowired
    public PublicHolidaysServiceImpl(Map<String, HolidayManager> holidayManagers) {
        this.holidayManagers = holidayManagers;
    }

    @Override
    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState) {
        requireCalculableDuration(new DateRange(from, to));

        final Locale locale = LocaleContextHolder.getLocale();
        final HolidayManager holidayManager = holidayManagers.get(federalState.getCountry());
        if (holidayManager == null) {
            return List.of();
        }

        return holidayManager.getHolidays(from, to, PUBLIC_HOLIDAY, federalState.getCodes()).stream()
            .map(holiday -> new PublicHoliday(holiday.getDate(), FULL, holiday.getDescription(locale)))
            .toList();
    }
}
