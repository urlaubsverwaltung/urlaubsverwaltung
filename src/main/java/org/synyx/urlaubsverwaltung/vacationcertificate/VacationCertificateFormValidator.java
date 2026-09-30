package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class VacationCertificateFormValidator implements Validator {

    private static final String EMPLOYMENT_FROM = "employmentFrom";
    private static final String EMPLOYMENT_TO = "employmentTo";
    private static final String COMPENSATED_DAYS = "compensatedDays";

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_ENTRY_MIN = "error.entry.min";
    private static final String ERROR_FULL_OR_HALF_NUMBER = "error.entry.fullOrHalfNumber";

    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return VacationCertificateFormDto.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        final VacationCertificateFormDto form = (VacationCertificateFormDto) target;
        validateEmployment(form, errors);
        validateCompensatedDays(form, errors);
    }

    private static void validateEmployment(VacationCertificateFormDto form, Errors errors) {

        final LocalDate employmentFrom = form.getEmploymentFrom();
        final LocalDate employmentTo = form.getEmploymentTo();

        if (employmentFrom == null) {
            reject(errors, EMPLOYMENT_FROM, ERROR_MANDATORY_FIELD);
        }
        if (employmentTo == null) {
            reject(errors, EMPLOYMENT_TO, ERROR_MANDATORY_FIELD);
        }
        if (employmentFrom == null || employmentTo == null) {
            return;
        }

        if (employmentFrom.isAfter(employmentTo)) {
            reject(errors, EMPLOYMENT_TO, msg("employmentTo.beforeFrom"));
            return;
        }

        final int year = form.getYear();
        if (employmentFrom.getYear() > year) {
            reject(errors, EMPLOYMENT_FROM, msg("employmentFrom.afterYear"), String.valueOf(year));
        }
        if (employmentTo.getYear() < year) {
            reject(errors, EMPLOYMENT_TO, msg("employmentTo.beforeYear"), String.valueOf(year));
        }
    }

    private static void validateCompensatedDays(VacationCertificateFormDto form, Errors errors) {

        final BigDecimal compensatedDays = form.getCompensatedDays();

        if (compensatedDays == null) {
            reject(errors, COMPENSATED_DAYS, ERROR_MANDATORY_FIELD);
        } else if (compensatedDays.signum() < 0) {
            reject(errors, COMPENSATED_DAYS, ERROR_ENTRY_MIN, "0");
        } else if (compensatedDays.remainder(HALF_DAY).signum() != 0) {
            reject(errors, COMPENSATED_DAYS, ERROR_FULL_OR_HALF_NUMBER);
        }
    }

    private static void reject(Errors errors, String field, String errorCode, Object... messageAttributes) {
        if (errors.hasFieldErrors(field)) {
            return;
        }
        if (messageAttributes.length == 0) {
            errors.rejectValue(field, errorCode);
        } else {
            errors.rejectValue(field, errorCode, messageAttributes, "");
        }
    }

    private static String msg(String key) {
        return "vacationcertificate.form.error." + key;
    }
}
