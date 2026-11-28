package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

class VacationCertificateFormValidatorTest {

    private final VacationCertificateFormValidator sut = new VacationCertificateFormValidator();

    @Test
    void ensureSupportsTheForm() {
        assertThat(sut.supports(VacationCertificateFormDto.class)).isTrue();
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureValidFormHasNoErrors() {
        final Errors errors = validate(validForm());
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void ensureEmploymentFromIsMandatory() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentFrom(null);

        assertThat(validate(form).getFieldError("employmentFrom").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureEmploymentToIsMandatory() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentTo(null);

        assertThat(validate(form).getFieldError("employmentTo").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureNoSecondErrorOnFieldWithBindingError() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentTo(null);
        final Errors errors = new BeanPropertyBindingResult(form, "certificateForm");
        errors.rejectValue("employmentTo", "typeMismatch");

        sut.validate(form, errors);

        assertThat(errors.getFieldErrorCount("employmentTo")).isOne();
    }

    @Test
    void ensureEmploymentToMustNotBeBeforeEmploymentFrom() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentFrom(LocalDate.of(2026, JUNE, 30));
        form.setEmploymentTo(LocalDate.of(2026, JUNE, 29));

        assertThat(validate(form).getFieldError("employmentTo").getCode()).isEqualTo("vacationcertificate.form.error.employmentTo.beforeFrom");
    }

    @Test
    void ensureEmploymentMustNotStartAfterTheYear() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentFrom(LocalDate.of(2027, JANUARY, 1));
        form.setEmploymentTo(LocalDate.of(2027, JUNE, 30));

        final Errors errors = validate(form);
        assertThat(errors.getFieldError("employmentFrom").getCode()).isEqualTo("vacationcertificate.form.error.employmentFrom.afterYear");
        assertThat(errors.getFieldError("employmentFrom").getArguments()).containsExactly("2026");
    }

    @Test
    void ensureEmploymentMustNotEndBeforeTheYear() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentFrom(LocalDate.of(2020, JANUARY, 1));
        form.setEmploymentTo(LocalDate.of(2025, DECEMBER, 31));

        final Errors errors = validate(form);
        assertThat(errors.getFieldError("employmentTo").getCode()).isEqualTo("vacationcertificate.form.error.employmentTo.beforeYear");
        assertThat(errors.getFieldError("employmentTo").getArguments()).containsExactly("2026");
    }

    @Test
    void ensureEmploymentOnTheFirstDayOfTheYearIsValid() {
        final VacationCertificateFormDto form = validForm();
        form.setEmploymentFrom(LocalDate.of(2025, JANUARY, 1));
        form.setEmploymentTo(LocalDate.of(2026, JANUARY, 1));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    @Test
    void ensureCompensatedDaysAreMandatory() {
        final VacationCertificateFormDto form = validForm();
        form.setCompensatedDays(null);

        assertThat(validate(form).getFieldError("compensatedDays").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureCompensatedDaysMustNotBeNegative() {
        final VacationCertificateFormDto form = validForm();
        form.setCompensatedDays(new BigDecimal("-1"));

        assertThat(validate(form).getFieldError("compensatedDays").getCode()).isEqualTo("error.entry.min");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.25", "1.7"})
    void ensureCompensatedDaysAreFullOrHalfDays(String days) {
        final VacationCertificateFormDto form = validForm();
        form.setCompensatedDays(new BigDecimal(days));

        assertThat(validate(form).getFieldError("compensatedDays").getCode()).isEqualTo("error.entry.fullOrHalfNumber");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.5", "12", "12.50"})
    void ensureValidCompensatedDays(String days) {
        final VacationCertificateFormDto form = validForm();
        form.setCompensatedDays(new BigDecimal(days));

        assertThat(validate(form).hasErrors()).isFalse();
    }

    private Errors validate(VacationCertificateFormDto form) {
        final Errors errors = new BeanPropertyBindingResult(form, "certificateForm");
        sut.validate(form, errors);
        return errors;
    }

    private static VacationCertificateFormDto validForm() {
        final VacationCertificateFormDto form = new VacationCertificateFormDto();
        form.setYear(2026);
        form.setEmploymentFrom(LocalDate.of(2019, JANUARY, 1));
        form.setEmploymentTo(LocalDate.of(2026, JUNE, 30));
        form.setCompensatedDays(BigDecimal.ZERO);
        return form;
    }
}
