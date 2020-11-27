package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validates {@link AccountForm}.
 */
@Component
class AccountFormValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INTEGER_FIELD = "error.entry.integer";
    private static final String ERROR_ENTRY = "error.entry.invalid";
    private static final String ERROR_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_COMMENT_TO_LONG = "error.entry.commentTooLong";
    private static final String ERROR_FULL_OR_HALF_AN_HOUR_FIELD = "error.entry.fullOrHalfHour";

    private static final String ATTRIBUTE_ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String ATTRIBUTE_ACTUAL_VACATION_DAYS = "actualVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING = "remainingVacationDaysNotExpiring";
    private static final String ATTRIBUTE_COMMENT = "comment";

    private final SettingsService settingsService;

    @Autowired
    AccountFormValidator(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final BigDecimal maximumAnnualVacationDays = getMaximumAnnualVacationDays();

        final AccountForm form = (AccountForm) target;
        validatePeriod(form, errors);
        validateAnnualVacation(form, errors, maximumAnnualVacationDays);
        validateActualVacation(form, errors);
        validateRemainingVacationDays(form, errors, maximumAnnualVacationDays);
        validateComment(form, errors);
    }

    void validateComment(AccountForm form, Errors errors) {

        final String comment = form.getComment();
        if (comment != null && comment.length() > 200) {
            errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_COMMENT_TO_LONG);
        }
    }

    void validatePeriod(AccountForm form, Errors errors) {

        final LocalDate holidaysAccountValidFrom = form.getHolidaysAccountValidFrom();
        final LocalDate holidaysAccountValidTo = form.getHolidaysAccountValidTo();

        validateDateNotNull(holidaysAccountValidFrom, "holidaysAccountValidFrom", errors);
        validateDateNotNull(holidaysAccountValidTo, "holidaysAccountValidTo", errors);

        if (holidaysAccountValidFrom != null && holidaysAccountValidTo != null) {
            boolean periodIsNotWithinOneYear = holidaysAccountValidFrom.getYear() != form.getHolidaysAccountYear()
                || holidaysAccountValidTo.getYear() != form.getHolidaysAccountYear();
            boolean periodIsOnlyOneDay = holidaysAccountValidFrom.equals(holidaysAccountValidTo);
            boolean beginOfPeriodIsAfterEndOfPeriod = holidaysAccountValidFrom.isAfter(holidaysAccountValidTo);

            if (periodIsNotWithinOneYear || periodIsOnlyOneDay || beginOfPeriodIsAfterEndOfPeriod) {
                errors.reject(ERROR_PERIOD);
            }
        }
    }

    void validateAnnualVacation(AccountForm form, Errors errors, BigDecimal maxDays) {

        final BigDecimal annualVacationDays = form.getAnnualVacationDays();
        validateNumberNotNull(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);

        if (annualVacationDays != null) {

            validateIsInteger(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);
            validateNumberOfDays(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, maxDays, errors);
        }
    }

    void validateActualVacation(AccountForm form, Errors errors) {

        final BigDecimal actualVacationDays = form.getActualVacationDays();
        validateNumberNotNull(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

        if (actualVacationDays != null) {

            validateFullOrHalfAnHour(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

            final BigDecimal annualVacationDays = form.getAnnualVacationDays();
            validateNumberOfDays(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, annualVacationDays, errors);
        }
    }

    void validateRemainingVacationDays(AccountForm form, Errors errors, BigDecimal maxDays) {

        final BigDecimal remainingVacationDays = form.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = form.getRemainingVacationDaysNotExpiring();

        validateNumberNotNull(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);
        validateNumberNotNull(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, errors);

        if (remainingVacationDays != null) {
            // field entitlement's remaining vacation days
            validateFullOrHalfAnHour(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);
            validateNumberOfDays(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, maxDays, errors);

            if (remainingVacationDaysNotExpiring != null) {
                validateFullOrHalfAnHour(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, errors);
                validateNumberOfDays(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, remainingVacationDays, errors);
            }
        }
    }

    private void validateFullOrHalfAnHour(BigDecimal days, String field, Errors errors) {

        final String decimal = days.subtract(new BigDecimal(days.intValue())).toPlainString();
        final boolean isFullOrHalfAnHour = decimal.equals("0") || decimal.startsWith("0.0") || decimal.startsWith("0.5");

        if (!isFullOrHalfAnHour && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_FULL_OR_HALF_AN_HOUR_FIELD);
        }
    }

    private void validateDateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validateIsInteger(BigDecimal days, String field, Errors errors) {

        final String decimal = days.subtract(new BigDecimal(days.intValue())).toPlainString();
        final boolean isValid = decimal.startsWith("0.0") || decimal.equals("0");

        if (!isValid && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_INTEGER_FIELD);
        }
    }

    private void validateNumberNotNull(BigDecimal number, String field, Errors errors) {

        // may be that number field is null because of cast exception, than there is already a field error
        final boolean isValid = number != null;

        if (!isValid && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validateNumberOfDays(BigDecimal days, String field, BigDecimal maximumDays, Errors errors) {

        // is number of days < 0 ?
        if (days.compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue(field, ERROR_ENTRY);
        }

        // is number of days unrealistic?
        if (days.compareTo(maximumDays) > 0) {
            errors.rejectValue(field, ERROR_ENTRY);
        }
    }

    private BigDecimal getMaximumAnnualVacationDays() {
        final AccountSettings accountSettings = settingsService.getSettings().getAccountSettings();
        return BigDecimal.valueOf(accountSettings.getMaximumAnnualVacationDays());
    }
}
