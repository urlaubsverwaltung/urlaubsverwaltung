package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
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
    private static final String ERROR_COMMENT_TO_LONG = "error.entry.commentTooLong";
    private static final String ERROR_FULL_OR_HALF_NUMBER = "error.entry.fullOrHalfNumber";

    private static final String ATTRIBUTE_ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String ATTRIBUTE_ACTUAL_VACATION_DAYS = "actualVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING = "remainingVacationDaysNotExpiring";
    private static final String ATTRIBUTE_COMMENT = "comment";
    private static final String ATTR_HOLIDAYS_ACCOUNT_VALID_FROM = "holidaysAccountValidFrom";
    private static final String ATTR_HOLIDAYS_ACCOUNT_VALID_TO = "holidaysAccountValidTo";
    private static final String ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE = "expiryDate";
    private static final String ERROR_ENTRY_MIN = "error.entry.min";

    private final SettingsService settingsService;

    @Autowired
    AccountFormValidator(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return AccountForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        final BigDecimal maxAnnualVacationDays = getMaximumAnnualVacationDays();

        final AccountForm form = (AccountForm) target;
        validatePeriod(form, errors);
        validateAnnualVacation(form, errors, maxAnnualVacationDays);
        validateActualVacation(form, errors);
        validateExpiryDate(form, errors);
        validateRemainingVacationDays(form, errors, maxAnnualVacationDays);
        validateRemainingVacationDaysNotExpiring(form, errors);
        validateComment(form, errors);
    }

    void validatePeriod(AccountForm form, Errors errors) {

        final LocalDate holidaysAccountValidFrom = form.getHolidaysAccountValidFrom();
        final LocalDate holidaysAccountValidTo = form.getHolidaysAccountValidTo();

        validateDateNotNull(holidaysAccountValidFrom, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, errors);
        validateDateNotNull(holidaysAccountValidTo, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, errors);

        if (holidaysAccountValidFrom != null && holidaysAccountValidTo != null) {
            final int year = form.getHolidaysAccountYear();
            final int fromYear = holidaysAccountValidFrom.getYear();
            final int toYear = holidaysAccountValidTo.getYear();
            final boolean invalidYear = fromYear != year || toYear != year;

            if (invalidYear) {
                if (fromYear != year) {
                    reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidYear"), String.valueOf(year));
                }
                if (toYear != year) {
                    reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidYear"), String.valueOf(year));
                }
            }

            boolean periodIsOnlyOneDay = holidaysAccountValidFrom.equals(holidaysAccountValidTo);
            if (periodIsOnlyOneDay) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidRange"));
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidRange"));
            }

            boolean beginOfPeriodIsAfterEndOfPeriod = holidaysAccountValidFrom.isAfter(holidaysAccountValidTo);
            if (beginOfPeriodIsAfterEndOfPeriod) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_FROM, msg("holidaysAccountValidFrom.invalidRangeReversed"));
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_VALID_TO, msg("holidaysAccountValidTo.invalidRangeReversed"));
            }
        }
    }

    void validateAnnualVacation(AccountForm form, Errors errors, BigDecimal maxAnnualVacationDays) {

        final BigDecimal annualVacationDays = form.getAnnualVacationDays();
        validateNumberNotNull(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);

        if (annualVacationDays != null) {

            if (isAllowHalfDaysActive()) {
                validateFullOrHalfDay(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);
            } else {
                validateIsInteger(annualVacationDays, ATTRIBUTE_ANNUAL_VACATION_DAYS, errors);
            }

            if (isNegative(annualVacationDays)) {
                reject(errors, ATTRIBUTE_ANNUAL_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            } else if (isGreater(annualVacationDays, maxAnnualVacationDays)) {
                reject(errors, ATTRIBUTE_ANNUAL_VACATION_DAYS, "error.entry.max", asIntString(maxAnnualVacationDays));
            }
        }
    }

    void validateActualVacation(AccountForm form, Errors errors) {

        final BigDecimal actualVacationDays = form.getActualVacationDays();
        validateNumberNotNull(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

        if (actualVacationDays != null) {

            validateFullOrHalfDay(actualVacationDays, ATTRIBUTE_ACTUAL_VACATION_DAYS, errors);

            final BigDecimal annualVacationDays = form.getAnnualVacationDays();

            if (isNegative(actualVacationDays)) {
                reject(errors, ATTRIBUTE_ACTUAL_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            } else if (isGreater(actualVacationDays, annualVacationDays)) {
                reject(errors, ATTRIBUTE_ACTUAL_VACATION_DAYS, "error.entry.max", asIntString(annualVacationDays));
            }
        }
    }

    void validateExpiryDate(AccountForm form, Errors errors) {

        final LocalDate expiryDate = form.getExpiryDate();
        validateDateNotNull(expiryDate, ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE, errors);

        if (expiryDate != null) {
            final int year = form.getHolidaysAccountYear();
            final int expiryYear = expiryDate.getYear();

            if (expiryYear != year) {
                reject(errors, ATTR_HOLIDAYS_ACCOUNT_EXPIRY_DATE, msg("expiryDate.invalidYear"), String.valueOf(year));
            }
        }
    }

    void validateRemainingVacationDays(AccountForm form, Errors errors, BigDecimal maxAnnualVacationDays) {

        final BigDecimal remainingVacationDays = form.getRemainingVacationDays();

        validateNumberNotNull(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);

        if (remainingVacationDays != null) {
            validateFullOrHalfDay(remainingVacationDays, ATTRIBUTE_REMAINING_VACATION_DAYS, errors);
            if (isNegative(remainingVacationDays)) {
                reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS, ERROR_ENTRY_MIN, "0");
            } else if (isGreater(remainingVacationDays, maxAnnualVacationDays)) {
                reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS, msg("remainingVacationDays.tooBig"), asIntString(maxAnnualVacationDays));
            }
        }
    }

    void validateRemainingVacationDaysNotExpiring(AccountForm form, Errors errors) {

        final BigDecimal remainingVacationDays = form.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = form.getRemainingVacationDaysNotExpiring();

        if (remainingVacationDays == null && remainingVacationDaysNotExpiring == null) {
            reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_MANDATORY_FIELD);
        }

        if (remainingVacationDays != null) {
            if (remainingVacationDaysNotExpiring == null) {
                reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_MANDATORY_FIELD);
            } else {
                validateFullOrHalfDay(remainingVacationDaysNotExpiring, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, errors);
                if (isNegative(remainingVacationDaysNotExpiring)) {
                    reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, ERROR_ENTRY_MIN, "0");
                } else if (isGreater(remainingVacationDaysNotExpiring, remainingVacationDays)) {
                    reject(errors, ATTRIBUTE_REMAINING_VACATION_DAYS_NOT_EXPIRING, msg("remainingVacationDaysNotExpiring.tooBig"), asIntString(remainingVacationDays));
                }
            }
        }
    }

    void validateComment(AccountForm form, Errors errors) {

        final String comment = form.getComment();
        if (comment != null && comment.length() > 200) {
            errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_COMMENT_TO_LONG);
        }
    }

    private void validateFullOrHalfDay(BigDecimal days, String field, Errors errors) {

        final int scale = days.stripTrailingZeros().scale();
        final String decimal = days.subtract(new BigDecimal(days.intValue())).toPlainString();
        final boolean isFullOrHalfAnHour = scale <= 1 && (decimal.equals("0") || decimal.startsWith("0.0") || decimal.startsWith("0.5"));

        if (!isFullOrHalfAnHour && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_FULL_OR_HALF_NUMBER);
        }
    }

    private void validateDateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && errors.getFieldErrors(field).isEmpty()) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        }
    }

    private void validateIsInteger(BigDecimal days, String field, Errors errors) {

        final boolean isValid = days.stripTrailingZeros().scale() <= 0;

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

    private boolean isNegative(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) < 0;
    }

    private boolean isGreater(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) > 0;
    }

    private void reject(Errors errors, String field, String errorCode, Object... messageAttributes) {
        if (messageAttributes.length == 0) {
            errors.rejectValue(field, errorCode);
        } else {
            errors.rejectValue(field, errorCode, messageAttributes, "");
        }
    }

    private static String asIntString(BigDecimal bigDecimal) {
        return String.valueOf(bigDecimal.intValue());
    }

    private static String msg(String key) {
        return "person.form.annualVacation.error." + key;
    }

    private BigDecimal getMaximumAnnualVacationDays() {
        final AccountSettings accountSettings = settingsService.getSettings().getAccountSettings();
        return BigDecimal.valueOf(accountSettings.getMaximumAnnualVacationDays());
    }

    private boolean isAllowHalfDaysActive() {
        final ApplicationSettings applicationSettings = settingsService.getSettings().getApplicationSettings();
        return applicationSettings.isAllowHalfDays();
    }
}
