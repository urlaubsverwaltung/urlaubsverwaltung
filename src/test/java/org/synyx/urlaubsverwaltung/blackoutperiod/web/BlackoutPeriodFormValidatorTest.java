package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutPeriodFormValidatorTest {

    private final BlackoutPeriodFormValidator sut = new BlackoutPeriodFormValidator();

    @Test
    void ensureSupportsBlackoutPeriodFormClass() {
        assertThat(sut.supports(BlackoutPeriodForm.class)).isTrue();
    }

    @Test
    void ensureDoesNotSupportOtherClass() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureTitleIsMandatory() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now());

        final Errors errors = validate(form);

        assertThat(errors.getFieldError("title").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateIsMandatory() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setTitle("Jahresabschluss");
        form.setEndDate(LocalDate.now());

        final Errors errors = validate(form);

        assertThat(errors.getFieldError("startDate").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureEndDateIsMandatory() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setTitle("Jahresabschluss");
        form.setStartDate(LocalDate.now());

        final Errors errors = validate(form);

        assertThat(errors.getFieldError("endDate").getCode()).isEqualTo("error.entry.mandatory");
    }

    @Test
    void ensureStartDateMustBeBeforeEndDate() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setTitle("Jahresabschluss");
        form.setStartDate(LocalDate.of(2026, 12, 24));
        form.setEndDate(LocalDate.of(2026, 12, 20));

        final Errors errors = validate(form);

        assertThat(errors.getGlobalError().getCode()).isEqualTo("error.entry.invalidPeriod");
    }

    @Test
    void ensureValidFormHasNoErrors() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setTitle("Jahresabschluss");
        form.setStartDate(LocalDate.of(2026, 12, 20));
        form.setEndDate(LocalDate.of(2027, 1, 5));

        final Errors errors = validate(form);

        assertThat(errors.hasErrors()).isFalse();
    }

    private Errors validate(BlackoutPeriodForm form) {
        final Errors errors = new BeanPropertyBindingResult(form, "blackoutPeriod");
        sut.validate(form, errors);
        return errors;
    }
}
