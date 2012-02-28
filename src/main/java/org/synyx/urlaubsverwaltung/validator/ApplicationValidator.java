/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.apache.log4j.Logger;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.util.PropertiesUtil;
import org.synyx.urlaubsverwaltung.view.AppForm;

import java.util.Properties;


/**
 * This class validate if an application's form ('AppForm') is filled correctly by the user, else it saves error
 * messages in errors object.
 *
 * @author  Aljona Murygina
 */
public class ApplicationValidator implements Validator {

    // logs general errors like "properties file not found"
    private static final Logger LOG = Logger.getLogger("errorLog");

    // errors' properties keys
    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_REASON = "error.reason";
    private static final String ERROR_PERIOD = "error.period";
    private static final String ERROR_PAST = "error.period.past";
    private static final String ERROR_LENGTH = "error.length";

    // names of fields
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String START_DATE_HALF = "startDateHalf";
    private static final String REASON = "reason";
    private static final String ADDRESS = "address";
    private static final String PHONE = "phone";
    private static final String TEXT = "text";

    private static final String CUSTOM_PROPERTIES_FILE = "custom.properties";
    private Properties customProperties;
    private PropertiesValidator propValidator;

    public ApplicationValidator(PropertiesValidator propValidator) {

        this.propValidator = propValidator;

        try {
            this.customProperties = PropertiesUtil.load(CUSTOM_PROPERTIES_FILE);
        } catch (Exception ex) {
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
        if (app.getHowLong() == DayLength.FULL) {
            // check if date fields are not filled
            if (app.getStartDate() == null) {
                if (errors.getFieldErrors(START_DATE).isEmpty()) {
                    errors.rejectValue(START_DATE, MANDATORY_FIELD);
                }
            }

            if (app.getEndDate() == null) {
                if (errors.getFieldErrors(END_DATE).isEmpty()) {
                    errors.rejectValue(END_DATE, MANDATORY_FIELD);
                }
            }

            if (app.getStartDate() != null && app.getEndDate() != null) {
                // check if from < to
                if (app.getStartDate().isAfter(app.getEndDate())) {
                    errors.reject(ERROR_PERIOD);
                } else {
                    // applying for leave maximum permissible x months in advance
                    propValidator.validateMaximumVacationProperty(customProperties, app, errors);
                }
            }
        } else {
            if (app.getStartDateHalf() == null) {
                if (errors.getFieldErrors(START_DATE_HALF).isEmpty()) {
                    errors.rejectValue(START_DATE_HALF, MANDATORY_FIELD);
                }
            }
        }

        // check if reason is not filled
        if (app.getVacationType() != VacationType.HOLIDAY) {
            if (!StringUtils.hasText(app.getReason())) {
                errors.rejectValue(REASON, MANDATORY_FIELD);
            }
        }

        validateStringFields(app, errors);
    }


    /**
     * If a user (or a boss) applies for leave, startDate has to be equal or greater than now(). The office is able to
     * apply for leave for someone else and may apply for past too.
     *
     * @param  target
     * @param  errors
     */
    public void validateForUser(Object target, Errors errors) {

        AppForm app = (AppForm) target;

        // check if given dates are valid
        if (app.getHowLong() == DayLength.FULL) {
            if (app.getStartDate() != null) {
                if (app.getStartDate().isBeforeNow()) {
                    errors.reject(ERROR_PAST);
                }
            }
        } else {
            if (app.getStartDateHalf() != null) {
                if (app.getStartDateHalf().isBeforeNow()) {
                    errors.reject(ERROR_PAST);
                }
            }
        }
    }


    /**
     * Validates if comment field is filled (mandatory if application is rejected by boss).
     *
     * @param  target
     * @param  errors
     */
    public void validateComment(Object target, Errors errors) {

        Comment comment = (Comment) target;

        if (StringUtils.hasText(comment.getText())) {
            if (!validateStringLength(comment.getText())) {
                errors.rejectValue(TEXT, ERROR_LENGTH);
            }
        } else {
            errors.rejectValue(TEXT, ERROR_REASON);
        }
    }


    /**
     * Check if String fields of application form have a valid length.
     *
     * @param  target
     * @param  errors
     */
    private void validateStringFields(AppForm app, Errors errors) {

        if (StringUtils.hasText(app.getReason())) {
            if (!validateStringLength(app.getReason())) {
                errors.rejectValue(REASON, ERROR_LENGTH);
            }
        }

        if (StringUtils.hasText(app.getAddress())) {
            if (!validateStringLength(app.getAddress())) {
                errors.rejectValue(ADDRESS, ERROR_LENGTH);
            }
        }

        if (StringUtils.hasText(app.getPhone())) {
            if (!validateStringLength(app.getPhone())) {
                errors.rejectValue(PHONE, ERROR_LENGTH);
            }
        }
    }


    /**
     * Checks if a String has a valid length.
     *
     * @param  string
     *
     * @return
     */
    protected boolean validateStringLength(String string) {

        if (string.length() > 50) {
            return false;
        } else {
            return true;
        }
    }
}
