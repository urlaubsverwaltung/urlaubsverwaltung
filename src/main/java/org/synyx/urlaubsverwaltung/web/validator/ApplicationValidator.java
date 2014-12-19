package org.synyx.urlaubsverwaltung.web.validator;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;
import org.synyx.urlaubsverwaltung.web.application.AppForm;

import java.io.IOException;

import java.util.Properties;


/**
 * This class validate if an {@link AppForm} is filled correctly by the user, else it saves error messages in errors
 * object.
 *
 * @author  Aljona Murygina
 */
@Component
public class ApplicationValidator implements Validator {

    private static final Logger LOG = Logger.getLogger(ApplicationValidator.class);

    private static final int MAX_CHARS = 200;

    private static final String ERROR_MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_PERIOD = "error.period";
    private static final String ERROR_PAST = "error.period.past";
    private static final String ERROR_LENGTH = "error.length";
    private static final String ERROR_TOO_LONG = "error.too.long";

    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_START_DATE_HALF = "startDateHalf";
    private static final String FIELD_REASON = "reason";
    private static final String FIELD_ADDRESS = "address";

    private static final String BUSINESS_PROPERTIES_FILE = "business.properties";
    private static final String MAX_MONTHS_PROPERTY = "maximum.months";

    private Properties businessProperties;

    public ApplicationValidator() {

        try {
            this.businessProperties = PropertiesUtil.load(BUSINESS_PROPERTIES_FILE);
        } catch (IOException ex) {
            LOG.error("No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return AppForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        AppForm app = (AppForm) target;

        // check if date fields are valid
        validateDateFields(app, errors);

        // check if reason is not filled
        if (app.getVacationType() != VacationType.HOLIDAY) {
            if (!StringUtils.hasText(app.getReason())) {
                errors.rejectValue(FIELD_REASON, ERROR_MANDATORY_FIELD);
            }
        }

        validateStringLength(app.getReason(), FIELD_REASON, errors);
        validateStringLength(app.getAddress(), FIELD_ADDRESS, errors);
        validateStringLength(app.getComment(), "comment", errors);
    }


    private void validateDateFields(AppForm applicationForLeave, Errors errors) {

        if (applicationForLeave.getHowLong() == DayLength.FULL) {
            DateMidnight startDate = applicationForLeave.getStartDate();
            DateMidnight endDate = applicationForLeave.getEndDate();

            validateNotNull(startDate, FIELD_START_DATE, errors);
            validateNotNull(endDate, FIELD_END_DATE, errors);

            if (startDate != null && endDate != null) {
                validatePeriod(startDate, endDate, errors);
            }
        } else {
            DateMidnight date = applicationForLeave.getStartDateHalf();

            validateNotNull(date, FIELD_START_DATE_HALF, errors);
            validatePeriod(date, date, errors);
        }
    }


    private void validateNotNull(DateMidnight date, String field, Errors errors) {

        if (date == null) {
            // may be that date field is null because of cast exception, than there is already a field error
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, ERROR_MANDATORY_FIELD);
            }
        }
    }


    private void validatePeriod(DateMidnight startDate, DateMidnight endDate, Errors errors) {

        // ensure that startDate < endDate
        if (startDate.isAfter(endDate)) {
            errors.reject(ERROR_PERIOD);
        } else {
            validateNotTooFarInTheFuture(endDate, errors);
            validateNotTooFarInThePast(startDate, errors);
        }
    }


    private void validateNotTooFarInTheFuture(DateMidnight date, Errors errors) {

        String maximumMonthsProperty = businessProperties.getProperty(MAX_MONTHS_PROPERTY);
        int maximumMonths = Integer.parseInt(maximumMonthsProperty);

        DateMidnight future = DateMidnight.now().plusMonths(maximumMonths);

        if (date.isAfter(future)) {
            errors.reject(ERROR_TOO_LONG);
        }
    }


    private void validateNotTooFarInThePast(DateMidnight date, Errors errors) {

        String maximumMonthsProperty = businessProperties.getProperty(MAX_MONTHS_PROPERTY);
        int maximumMonths = Integer.parseInt(maximumMonthsProperty);

        DateMidnight past = DateMidnight.now().minusMonths(maximumMonths);

        if (date.isBefore(past)) {
            errors.reject(ERROR_PAST);
        }
    }


    private void validateStringLength(String text, String field, Errors errors) {

        if (StringUtils.hasText(text)) {
            if (text.length() > MAX_CHARS) {
                errors.rejectValue(field, ERROR_LENGTH);
            }
        }
    }


    /**
     * Validation for converting sick note to vacation: {@link AppForm} only reason and vacation type must be validated.
     *
     * @param  app
     * @param  errors
     */
    public void validatedShortenedAppForm(AppForm app, Errors errors) {

        if (!StringUtils.hasText(app.getReason())) {
            errors.rejectValue(FIELD_REASON, ERROR_MANDATORY_FIELD);
        } else {
            validateStringLength(app.getReason(), FIELD_REASON, errors);
        }
    }
}
