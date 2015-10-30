/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.calendar;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
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
     * @param  date
     *
     * @return  true if the given date is a public holiday, else false
     */
    boolean isPublicHoliday(DateMidnight date) {

        Settings settings = settingsService.getSettings();

        return manager.isHoliday(date.toLocalDate(), settings.getFederalState().getCodes());
    }


    /**
     * Returns the working duration for a date: may be full day (1.0) for a non public holiday or zero (0.0) for a
     * public holiday. The working duration for Christmas Eve and New Year's Eve are configured in the business
     * properties; normally the working duration for these holidays is a half day (0.5)
     *
     * @param  date  to get working duration for
     *
     * @return  working duration of the given date
     */
    public BigDecimal getWorkingDurationOfDate(DateMidnight date) {

        Settings settings = settingsService.getSettings();

        if (isPublicHoliday(date)) {
            if (DateUtil.isChristmasEve(date)) {
                return settings.getWorkingDurationForChristmasEve().getDuration();
            } else if (DateUtil.isNewYearsEve(date)) {
                return settings.getWorkingDurationForNewYearsEve().getDuration();
            } else {
                return DayLength.ZERO.getDuration();
            }
        }

        return DayLength.FULL.getDuration();
    }


    public Set<Holiday> getHolidays(int year) {

        Settings settings = settingsService.getSettings();

        return manager.getHolidays(year, settings.getFederalState().getCodes());
    }


    public Set<Holiday> getHolidays(int year, final int month) {

        Set<Holiday> holidays = getHolidays(year);

        return holidays.stream().filter(byMonth(month)).collect(Collectors.toSet());
    }


    private Predicate<Holiday> byMonth(int month) {

        return holiday -> holiday.getDate().getMonthOfYear() == month;
    }
}
