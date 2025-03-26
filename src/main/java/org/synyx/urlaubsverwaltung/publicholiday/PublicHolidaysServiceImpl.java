package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.focus_shift.jollyday.core.HolidayType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;

@Service
public class PublicHolidaysServiceImpl implements PublicHolidaysService {

    private final Map<String, HolidayManager> holidayManagers;

    @Autowired
    public PublicHolidaysServiceImpl(Map<String, HolidayManager> holidayManagers) {
        this.holidayManagers = holidayManagers;
    }

    @Override
    public Optional<PublicHoliday> getPublicHoliday(LocalDate date, FederalState federalState) {
        return getPublicHolidays(date, date, federalState).stream().findFirst();
    }

    @Override
    public List<PublicHoliday> getPublicHolidays(LocalDate from, LocalDate to, FederalState federalState) {
        final Locale locale = LocaleContextHolder.getLocale();

        return getHolidays(from, to, federalState).stream()
            .map(holiday -> new PublicHoliday(holiday.getDate(), getHolidayDayLength(holiday.getDate(), federalState), holiday.getDescription(locale)))
            .toList();
    }

    private DayLength getHolidayDayLength(LocalDate date, FederalState federalState) {
        return isPublicHoliday(date, federalState) ? ZERO.getInverse() : FULL.getInverse();
    }

    private boolean isPublicHoliday(LocalDate date, FederalState federalState) {
        return getHolidayManager(federalState)
            .map(holidayManager -> holidayManager.isHoliday(date, federalState.getCodes()))
            .orElse(false);
    }

    private Set<Holiday> getHolidays(final LocalDate from, final LocalDate to, FederalState federalState) {
        return getHolidayManager(federalState)
            .map(holidayManager -> holidayManager.getHolidays(from, to, PUBLIC_HOLIDAY, federalState.getCodes()))
            .orElseGet(HashSet::new);
    }

    private Optional<HolidayManager> getHolidayManager(FederalState federalState) {
        return Optional.ofNullable(holidayManagers.get(federalState.getCountry()));
    }
}
