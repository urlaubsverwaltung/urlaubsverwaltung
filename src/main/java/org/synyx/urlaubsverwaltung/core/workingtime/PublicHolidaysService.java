package org.synyx.urlaubsverwaltung.core.workingtime;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.net.URL;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Service for calendar purpose using jollyday library.
 *
 * @author  Aljona Murygina
 */
@Component
public class PublicHolidaysService {

    private static final String HOLIDAY_DEFINITION_FILE = "Holidays_de.xml";

    private final HolidayManager manager;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidaysService(SettingsService settingsService) {

        this.settingsService = settingsService;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(HOLIDAY_DEFINITION_FILE);

        this.manager = HolidayManager.getInstance(url);
    }

    /**
     * Checks if the given date is a public holiday by lookup in the given set of public holidays.
     *
     * @param  date  to check if it is a public holiday
     * @param  federalState  the federal state to consider holiday settings for
     *
     * @return  true if the given date is a public holiday, else false
     */
    boolean isPublicHoliday(DateMidnight date, FederalState federalState) {

//we should check here if the federal state is given. Or not? I dont know it.
        return manager.isHoliday(date.toLocalDate(), federalState.getCodes());
    }


    /**
     * Returns the working duration for a date: may be full day (1.0) for a non public holiday or zero (0.0) for a
     * public holiday. The working duration for Christmas Eve and New Year's Eve are configured in the business
     * properties; normally the working duration for these holidays is a half day (0.5)
     *
     * @param  date  to get working duration for
     * @param  federalState  the federal state to consider holiday settings for
     *
     * @return  working duration of the given date
     */
    public BigDecimal getWorkingDurationOfDate(DateMidnight date, FederalState federalState) {

        Settings settings = settingsService.getSettings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        if (isPublicHoliday(date, federalState)) {
            if (DateUtil.isChristmasEve(date)) {
                return workingTimeSettings.getWorkingDurationForChristmasEve().getDuration();
            } else if (DateUtil.isNewYearsEve(date)) {
                return workingTimeSettings.getWorkingDurationForNewYearsEve().getDuration();
            } else {
                return DayLength.ZERO.getDuration();
            }
        }

        return DayLength.FULL.getDuration();
    }


    public Set<Holiday> getHolidays(int year, FederalState federalState) {

        return manager.getHolidays(year, federalState.getCodes());
    }


    public Set<Holiday> getHolidays(int year, final int month, FederalState federalState) {

        Set<Holiday> holidays = getHolidays(year, federalState);

        return holidays.stream().filter(byMonth(month)).collect(Collectors.toSet());
    }


    private Predicate<Holiday> byMonth(int month) {

        return holiday -> holiday.getDate().getMonthOfYear() == month;
    }
}
