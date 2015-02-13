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

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;

import java.io.IOException;

import java.math.BigDecimal;

import java.net.URL;

import java.util.Properties;
import java.util.Set;


/**
 * Service for calendar purpose using jollyday library.
 *
 * @author  Aljona Murygina
 */
@Component
public class JollydayCalendar {

    private static final String HOLIDAY_DEFINITION_FILE = "Holidays_custom.xml";

    private static final String BUSINESS_PROPERTIES_FILE = "business.properties";
    private static final String VACATION_DAY_COUNT_CONFIGURATION = "holiday.%s.vacationDay";
    private static final String CHRISTMAS_EVE_PROPERTY_KEY = "CHRISTMAS_EVE";
    private static final String NEW_YEARS_EVE_PROPERTY_KEY = "NEW_YEARS_EVE";

    private HolidayManager manager;
    private Properties businessProperties;

    public JollydayCalendar() throws IOException {

        this(PropertiesUtil.load(BUSINESS_PROPERTIES_FILE));
    }


    protected JollydayCalendar(Properties properties) {

        this.businessProperties = properties;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(HOLIDAY_DEFINITION_FILE);

        manager = HolidayManager.getInstance(url);
    }

    /**
     * Checks if the given date is a public holiday by lookup in the given set of public holidays.
     *
     * @param  date
     *
     * @return  true if the given date is a public holiday, else false
     */
    boolean isPublicHoliday(DateMidnight date) {

        if (manager.isHoliday(date.toLocalDate())) {
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

        if (isPublicHoliday(date)) {
            if (DateUtil.isChristmasEve(date)) {
                return getConfiguredVacationDayCountForHoliday(CHRISTMAS_EVE_PROPERTY_KEY);
            } else if (DateUtil.isNewYearsEve(date)) {
                return getConfiguredVacationDayCountForHoliday(NEW_YEARS_EVE_PROPERTY_KEY);
            } else {
                return DayLength.ZERO.getDuration();
            }
        }

        return DayLength.FULL.getDuration();
    }


    public Set<Holiday> getHolidays(int year) {

        return manager.getHolidays(year);
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


    /**
     * Returns the number of days that should be calculated for the given holiday.
     */
    private BigDecimal getConfiguredVacationDayCountForHoliday(String holidayName) {

        String propertyName = String.format(VACATION_DAY_COUNT_CONFIGURATION, holidayName);
        String vacationDayCountConfig = businessProperties.getProperty(propertyName);

        DayLength dayLength = DayLength.valueOf(vacationDayCountConfig);

        return dayLength.getDuration();
    }
}
