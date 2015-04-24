/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.calendar;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.net.URL;

import java.util.Set;


/**
 * Service for calendar purpose using jollyday library.
 *
 * @author  Aljona Murygina
 */
@Component
public class JollydayCalendar {

    private static final String HOLIDAY_DEFINITION_FILE = "Holidays_de.xml";

    private final HolidayManager manager;
    private final SettingsService settingsService;

    @Autowired
    public JollydayCalendar(SettingsService settingsService) {

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

        if (manager.isHoliday(date.toLocalDate(), settings.getFederalState().getCodes())) {
            return true;
        }

        return false;
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

        Iterable<Holiday> holidaysForMonth = Iterables.filter(holidays, new Predicate<Holiday>() {

                    @Override
                    public boolean apply(Holiday holiday) {

                        return holiday.getDate().getMonthOfYear() == month;
                    }
                });

        return Sets.newHashSet(holidaysForMonth);
    }
}
