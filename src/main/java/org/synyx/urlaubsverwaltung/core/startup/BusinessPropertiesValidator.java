/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.startup;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;

import java.io.IOException;

import java.util.Properties;

import javax.annotation.PostConstruct;


/**
 * Validates the business properties like maximum possible days of annual vacation and shuts down the application if the
 * business properties have invalid entries.
 *
 * @author  Aljona Murygina
 */
@Component
public class BusinessPropertiesValidator {

    static final String MAX_DAYS = "annual.vacation.max";
    static final String MAX_MONTHS = "maximum.months";
    static final String CHRISTMAS_EVE_PROPERTY_KEY = "holiday.CHRISTMAS_EVE.vacationDay";
    static final String NEW_YEARS_EVE_PROPERTY_KEY = "holiday.NEW_YEARS_EVE.vacationDay";

    private static final Logger LOG = Logger.getLogger(BusinessPropertiesValidator.class);

    private static final String BUSINESS_PROPERTIES_FILE = "business.properties";

    private final Runnable onError;

    private final Properties properties;

    public BusinessPropertiesValidator() throws IOException {

        this(new Runnable() {

                @Override
                public void run() {

                    LOG.error(" >>> Business properties failure");
                    LOG.error(" >>> Shutting down with system exit!");
                    LOG.error(" >>> Check your business properties in file: " + BUSINESS_PROPERTIES_FILE);
                    System.exit(1);
                }
            }, PropertiesUtil.load(BUSINESS_PROPERTIES_FILE));
    }


    BusinessPropertiesValidator(Runnable onError, Properties properties) {

        this.onError = onError;
        this.properties = properties;
    }

    // TODO: Validate business properties for sick note too
    @PostConstruct
    public void validateBusinessProperties() {

        if (!isAnnualVacationPropertyValid() || !isMaximumMonthsToApplyForLeaveInAdvancePropertyValid()
                || !isWorkingDurationPropertyValid()) {
            onError.run();
        }
    }


    boolean isAnnualVacationPropertyValid() {

        String annualVacationProperty = properties.getProperty(MAX_DAYS);

        try {
            int annualVacationDays = Integer.parseInt(annualVacationProperty);

            if (annualVacationDays >= 1 && annualVacationDays <= 366) {
                return true;
            }
        } catch (NumberFormatException ex) {
            LOG.error("Can not parse value for annual vacation property to a number: " + annualVacationProperty);
        }

        return false;
    }


    boolean isMaximumMonthsToApplyForLeaveInAdvancePropertyValid() {

        String maximumMonthsToApplyForLeaveInAdvanceProperty = properties.getProperty(MAX_MONTHS);

        try {
            int maximumMonthsToApplyForLeaveInAdvance = Integer.parseInt(maximumMonthsToApplyForLeaveInAdvanceProperty);

            if (maximumMonthsToApplyForLeaveInAdvance > 0 && maximumMonthsToApplyForLeaveInAdvance < 37) {
                return true;
            }
        } catch (NumberFormatException ex) {
            LOG.error("Can not parse value for maximum months to apply for leave in advance property to a number: "
                + maximumMonthsToApplyForLeaveInAdvanceProperty);
        }

        return false;
    }


    boolean isWorkingDurationPropertyValid() {

        String christmasEveWorkingDurationProperty = properties.getProperty(CHRISTMAS_EVE_PROPERTY_KEY);

        try {
            DayLength.valueOf(christmasEveWorkingDurationProperty);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String newYearsEveWorkingDurationProperty = properties.getProperty(NEW_YEARS_EVE_PROPERTY_KEY);

        try {
            DayLength.valueOf(newYearsEveWorkingDurationProperty);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return true;
    }

    // TODO: Validate values for working duration of Christmas Eve and New Years Eve
}
