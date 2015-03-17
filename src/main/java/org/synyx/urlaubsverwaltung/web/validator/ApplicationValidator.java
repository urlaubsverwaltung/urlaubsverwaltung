package org.synyx.urlaubsverwaltung.web.validator;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;
import org.synyx.urlaubsverwaltung.web.application.ApplicationForLeaveForm;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Properties;


/**
 * This class validate if an {@link org.synyx.urlaubsverwaltung.web.application.ApplicationForLeaveForm} is filled
 * correctly by the user, else it saves error messages in errors object.
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
    private static final String ERROR_ZERO_DAYS = "error.zero.days";
    private static final String ERROR_OVERLAP = "error.overlap";
    private static final String ERROR_NOT_ENOUGH_DAYS = "error.not.enough.days";

    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_START_DATE_HALF = "startDateHalf";
    private static final String FIELD_REASON = "reason";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_COMMENT = "comment";

    private static final String BUSINESS_PROPERTIES_FILE = "business.properties";
    private static final String MAX_MONTHS_PROPERTY = "maximum.months";

    private Properties businessProperties;

    private final OwnCalendarService calendarService;
    private final OverlapService overlapService;
    private final CalculationService calculationService;

    @Autowired
    public ApplicationValidator(OwnCalendarService calendarService, OverlapService overlapService,
        CalculationService calculationService) {

        this.calendarService = calendarService;
        this.overlapService = overlapService;
        this.calculationService = calculationService;

        try {
            this.businessProperties = PropertiesUtil.load(BUSINESS_PROPERTIES_FILE);
        } catch (IOException ex) {
            LOG.error("No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return ApplicationForLeaveForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        ApplicationForLeaveForm applicationForm = (ApplicationForLeaveForm) target;

        // check if date fields are valid
        validateDateFields(applicationForm, errors);

        // check if reason is not filled
        if (applicationForm.getVacationType() == VacationType.SPECIALLEAVE) {
            if (!StringUtils.hasText(applicationForm.getReason())) {
                errors.rejectValue(FIELD_REASON, ERROR_MANDATORY_FIELD);
            }
        }

        // validate length of texts
        validateStringLength(applicationForm.getReason(), FIELD_REASON, errors);
        validateStringLength(applicationForm.getAddress(), FIELD_ADDRESS, errors);
        validateStringLength(applicationForm.getComment(), FIELD_COMMENT, errors);

        if (!errors.hasErrors()) {
            // validate if applying for leave is possible
            // (check overlapping applications for leave, vacation days of the person etc.)
            validateIfApplyingForLeaveIsPossible(applicationForm, errors);
        }
    }


    private void validateDateFields(ApplicationForLeaveForm applicationForLeave, Errors errors) {

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

            if (date != null) {
                validatePeriod(date, date, errors);
            }
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


    private void validateIfApplyingForLeaveIsPossible(ApplicationForLeaveForm applicationForm, Errors errors) {

        DayLength dayLength = applicationForm.getHowLong();
        Person person = applicationForm.getPerson();

        BigDecimal days;

        if (dayLength == DayLength.FULL) {
            days = calendarService.getWorkDays(dayLength, applicationForm.getStartDate(), applicationForm.getEndDate(),
                    person);
        } else {
            days = calendarService.getWorkDays(dayLength, applicationForm.getStartDateHalf(),
                    applicationForm.getStartDateHalf(), person);
        }

        /**
         * Ensure that no one applies for leave for a vacation of 0 days
         */
        if (CalcUtil.isZero(days)) {
            errors.reject(ERROR_ZERO_DAYS);

            return;
        }

        /**
         * Ensure that there is no application for leave and no sick note in the same period
         */
        Application application = applicationForm.generateApplicationForLeave();
        OverlapCase overlap = overlapService.checkOverlap(application);

        boolean isOverlapping = overlap == OverlapCase.FULLY_OVERLAPPING || overlap == OverlapCase.PARTLY_OVERLAPPING;

        if (isOverlapping) {
            errors.reject(ERROR_OVERLAP);

            return;
        }

        /**
         * Ensure that the person has enough vacation days left if the vacation type is
         * {@link org.synyx.urlaubsverwaltung.core.application.domain.VacationType.HOLIDAY}
         */

        boolean isHoliday = applicationForm.getVacationType() == VacationType.HOLIDAY;

        if (isHoliday) {
            boolean enoughVacationDaysLeft = calculationService.checkApplication(
                    applicationForm.generateApplicationForLeave());

            if (!enoughVacationDaysLeft) {
                errors.reject(ERROR_NOT_ENOUGH_DAYS);
            }
        }
    }
}
